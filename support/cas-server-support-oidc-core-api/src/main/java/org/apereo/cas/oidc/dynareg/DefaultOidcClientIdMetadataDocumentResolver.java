package org.apereo.cas.oidc.dynareg;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcClientIdMetadataDocumentProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationRequestTranslator;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link DefaultOidcClientIdMetadataDocumentResolver}. It registers clients from their OAuth client ID metadata
 * documents (draft-ietf-oauth-client-id-metadata-document). The client id must be an {@code https} URL with a path and no
 * user information, query, fragment or dot segments. The document must name that exact URL as its {@code client_id},
 * carry a {@code client_name} and {@code redirect_uris}, use no shared secret ({@code token_endpoint_auth_method} is
 * {@code none} or {@code private_key_jwt} with inline {@code jwks}) and ask only for the authorization code and refresh
 * token grants. Redirect URIs must be {@code https}, or {@code http} on a loopback host, and are matched exactly.
 * Metadata that would make CAS call URLs chosen by the client from the server side ({@code jwks_uri},
 * {@code sector_identifier_uri}, back-channel endpoints) is refused, and post-logout redirect URIs are not used.
 * <p>
 * The client is saved in the service registry, translated like a dynamic client registration but evaluated after every
 * other service so that it never takes precedence over one an administrator defined, and fetched again once the
 * cache lifetime of the document, bounded by CAS settings, has passed. A failed refresh keeps the last valid document.
 * Each successful fetch pushes the expiration of the client to the retention period from then, after which CAS removes
 * it, and no more than the configured number of such clients are registered. A registered client whose id is a URL but
 * was not registered from a document is left alone.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultOidcClientIdMetadataDocumentResolver implements OidcClientIdMetadataDocumentResolver {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final Set<String> REFUSED_METADATA = Set.of("client_secret", "client_secret_expires_at", "jwks_uri",
        "sector_identifier_uri", "backchannel_client_notification_endpoint", "backchannel_logout_uri",
        "frontchannel_logout_uri", "initiate_login_uri", "request_uris");

    private static final Set<String> ALLOWED_GRANT_TYPES = Set.of(
        OAuth20GrantTypes.AUTHORIZATION_CODE.getType(), OAuth20GrantTypes.REFRESH_TOKEN.getType());

    private final CasReentrantLock lock = new CasReentrantLock();

    private final OidcClientIdMetadataDocumentProperties properties;

    private final ServicesManager servicesManager;

    private final OidcClientRegistrationRequestTranslator translator;

    private final StringSerializer<OidcClientRegistrationRequest> serializer;

    private final OidcClientIdMetadataDocumentFetcher fetcher;

    @Override
    public Optional<OidcRegisteredService> resolve(@Nullable final String clientId) {
        if (!properties.isEnabled() || !OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentUrl(clientId)) {
            return Optional.empty();
        }
        val registeredClient = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (registeredClient != null && !(registeredClient instanceof OidcRegisteredService)) {
            return Optional.empty();
        }
        val existing = findClient(Objects.requireNonNull(clientId));
        if (existing.isPresent() && (!OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentClient(existing.get())
            || !isRefreshDue(existing.get()))) {
            return existing;
        }
        try {
            val document = fetcher.fetch(validateClientId(clientId));
            val registrationRequest = validateDocument(document);
            return Optional.ofNullable(register(document, registrationRequest));
        } catch (final Throwable e) {
            LOGGER.warn("Unable to use the client ID metadata document of [{}]: [{}]", clientId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return existing;
        }
    }

    /**
     * Check the client id against the client identifier rules of the specification.
     *
     * @param clientId the client id
     * @return the client id as a URI
     * @throws Exception when the client id is not a valid document URL
     */
    protected URI validateClientId(final String clientId) throws Exception {
        val uri = new URI(clientId);
        val path = uri.getRawPath();
        if (!"https".equals(uri.getScheme()) || StringUtils.isBlank(uri.getHost()) || uri.getRawUserInfo() != null
            || uri.getRawQuery() != null || uri.getRawFragment() != null || StringUtils.isBlank(path)
            || Arrays.stream(path.split("/")).anyMatch(segment -> ".".equals(segment) || "..".equals(segment))) {
            throw new IllegalArgumentException("The client id is not a valid client ID metadata document URL");
        }
        return uri;
    }

    /**
     * Check the document against the rules of the specification and of CAS.
     *
     * @param document the fetched document
     * @return the document as a registration request
     * @throws Exception when the document cannot be used
     */
    protected OidcClientRegistrationRequest validateDocument(final OidcClientIdMetadataDocument document) throws Exception {
        val metadata = (Map<?, ?>) MAPPER.readValue(document.json(), Map.class);
        if (!document.clientId().equals(metadata.get("client_id"))) {
            throw new IllegalArgumentException("The client_id of the document does not match its URL");
        }
        val refused = REFUSED_METADATA.stream().filter(metadata::containsKey).toList();
        if (!refused.isEmpty()) {
            throw new IllegalArgumentException("The document carries metadata CAS does not accept: " + refused);
        }
        val registrationRequest = serializer.from(document.json());
        if (StringUtils.isBlank(registrationRequest.getClientName()) || registrationRequest.getRedirectUris().isEmpty()) {
            throw new IllegalArgumentException("The document must name the client and its redirect URIs");
        }
        if (!metadata.containsKey("token_endpoint_auth_method")) {
            registrationRequest.setTokenEndpointAuthMethod(OAuth20ClientAuthenticationMethods.NONE.getType());
        }
        val authMethod = registrationRequest.getTokenEndpointAuthMethod();
        val privateKeyJwt = OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT.getType().equals(authMethod);
        if (!privateKeyJwt && !OAuth20ClientAuthenticationMethods.NONE.getType().equals(authMethod)) {
            throw new IllegalArgumentException("The token endpoint authentication method must be none or private_key_jwt");
        }
        if (privateKeyJwt && (registrationRequest.getJwks() == null || registrationRequest.getJwks().getJsonWebKeys().isEmpty())) {
            throw new IllegalArgumentException("Clients that use private_key_jwt must publish their keys in jwks");
        }
        if (!ALLOWED_GRANT_TYPES.containsAll(registrationRequest.getGrantTypes())) {
            throw new IllegalArgumentException("Only the authorization_code and refresh_token grants are accepted");
        }
        if (!registrationRequest.getResponseTypes().stream().allMatch(OAuth20ResponseTypes.CODE.getType()::equals)) {
            throw new IllegalArgumentException("Only the code response type is accepted");
        }
        registrationRequest.getRedirectUris().forEach(DefaultOidcClientIdMetadataDocumentResolver::validateRedirectUri);
        registrationRequest.setPostLogoutRedirectUris(new ArrayList<>());
        registrationRequest.setBackchannelTokenDeliveryMode(null);
        return registrationRequest;
    }

    protected @Nullable OidcRegisteredService register(final OidcClientIdMetadataDocument document,
                                             final OidcClientRegistrationRequest registrationRequest) {
        return lock.executeAndThrow(() -> {
            val existing = findClient(document.clientId());
            if (existing.isEmpty() && countClients() >= properties.getMaximumClients()) {
                LOGGER.warn("Client [{}] is not registered: [{}] clients are already registered from client ID metadata documents",
                    document.clientId(), properties.getMaximumClients());
                return null;
            }
            val service = existing.orElseGet(() -> {
                val created = new OidcRegisteredService();
                created.setClientId(document.clientId());
                return created;
            });
            val registeredService = translator.translate(registrationRequest, Optional.of(service));
            registeredService.setClientId(document.clientId());
            registeredService.setClientSecrets(new ArrayList<>());
            registeredService.setServiceId(registrationRequest.getRedirectUris()
                .stream()
                .map(uri -> "\\Q" + uri + "\\E")
                .collect(Collectors.joining("|", "^(", ")$")));
            registeredService.setEvaluationOrder(Integer.MAX_VALUE);
            registeredService.setBypassApprovalPrompt(false);
            registeredService.setGenerateRefreshToken(registrationRequest.getGrantTypes()
                .contains(OAuth20GrantTypes.REFRESH_TOKEN.getType()));
            registeredService.setDescription("Registered from the client ID metadata document at " + document.clientId());

            val now = ZonedDateTime.now(ZoneOffset.UTC);
            registeredService.getProperties().put(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT.getPropertyName(),
                new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
            registeredService.getProperties().put(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT_REFRESH.getPropertyName(),
                new DefaultRegisteredServiceProperty(now.plus(cacheDuration(document)).toInstant().toString()));
            registeredService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
                now.plus(Beans.newDuration(properties.getRetention())).truncatedTo(ChronoUnit.SECONDS)));
            LOGGER.debug("Registering client [{}] from its client ID metadata document", document.clientId());
            return (OidcRegisteredService) servicesManager.save(registeredService);
        });
    }

    protected Duration cacheDuration(final OidcClientIdMetadataDocument document) {
        val minimum = Beans.newDuration(properties.getMinimumCacheDuration());
        val maximum = Beans.newDuration(properties.getMaximumCacheDuration());
        val lifetime = document.cacheLifetime();
        if (lifetime.compareTo(minimum) < 0) {
            return minimum;
        }
        return lifetime.compareTo(maximum) > 0 ? maximum : lifetime;
    }

    private static boolean isRefreshDue(final OidcRegisteredService registeredService) {
        val refresh = registeredService.getProperties()
            .get(RegisteredServiceProperties.OIDC_CLIENT_ID_METADATA_DOCUMENT_REFRESH.getPropertyName());
        if (refresh == null || StringUtils.isBlank(refresh.value())) {
            return true;
        }
        try {
            return !Instant.now(Clock.systemUTC()).isBefore(Instant.parse(refresh.value()));
        } catch (final DateTimeParseException e) {
            return true;
        }
    }

    private static void validateRedirectUri(final String redirectUri) {
        try {
            val uri = new URI(redirectUri);
            val loopback = "http".equals(uri.getScheme()) && LOOPBACK_HOSTS.contains(Objects.toString(uri.getHost(), StringUtils.EMPTY));
            if (!("https".equals(uri.getScheme()) || loopback) || StringUtils.isBlank(uri.getHost()) || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("Redirect URIs must be https, or http on a loopback host, without a fragment");
            }
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Redirect URI is not a valid URI", e);
        }
    }

    private Optional<OidcRegisteredService> findClient(final String clientId) {
        return Optional.ofNullable(OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId))
            .filter(OidcRegisteredService.class::isInstance)
            .map(OidcRegisteredService.class::cast);
    }

    private long countClients() {
        return servicesManager.getAllServicesOfType(OidcRegisteredService.class)
            .stream()
            .filter(OidcClientIdMetadataDocumentResolver::isClientIdMetadataDocumentClient)
            .count();
    }
}
