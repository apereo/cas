package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkValidator;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientRegistrationUtils;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRedirectUriUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceContact;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.util.JSONArrayUtils;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainSet;
import com.nimbusds.openid.connect.sdk.federation.trust.constraints.TrustChainConstraints;
import com.nimbusds.openid.connect.sdk.rp.ApplicationType;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcFederationDefaultTrustChainResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcFederationDefaultTrustChainResolver implements OidcFederationTrustChainResolver {

    /**
     * Tag to indicate that a service has been temporarily registered in case of an automatic registration.
     */
    public static final String TEMPORARY_OPENIDFEDERATION_SERVICE = "TEMPORARY_OPENIDFEDERATION_SERVICE";

    /**
     * Property of an automatically registered service: the time, in seconds since the epoch, at which the entity
     * configuration of the relying party that the registration was built from was issued.
     */
    public static final String OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT = "OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT";

    /**
     * Client authentication methods an automatically registered client may use: asymmetric cryptography is required,
     * since no secret is ever issued to it (OpenID Federation 1.0, section 12.1).
     */
    private static final Set<ClientAuthenticationMethod> ASYMMETRIC_CLIENT_AUTHENTICATION_METHODS =
        Set.of(ClientAuthenticationMethod.PRIVATE_KEY_JWT, ClientAuthenticationMethod.TLS_CLIENT_AUTH);

    private final List<TrustChainResolver> trustChainResolvers;

    private final OidcFederationTrustMarkValidator trustMarkValidator;

    private final OidcFederationResolutionLimiter resolutionLimiter;

    private final OidcFederationTrustAnchorKeys trustAnchorKeys;

    private final OidcFederationHttpClient httpClient;

    public OidcFederationDefaultTrustChainResolver(final List<TrustChainResolver> trustChainResolvers,
                                                   final OidcFederationTrustMarkValidator trustMarkValidator) {
        this(trustChainResolvers, trustMarkValidator, new OidcFederationResolutionLimiter(new OidcFederationProperties()));
    }

    public OidcFederationDefaultTrustChainResolver(final List<TrustChainResolver> trustChainResolvers,
                                                   final OidcFederationTrustMarkValidator trustMarkValidator,
                                                   final OidcFederationResolutionLimiter resolutionLimiter) {
        this(trustChainResolvers, trustMarkValidator, resolutionLimiter,
            new OidcFederationTrustAnchorKeys(new OidcFederationHttpClient(new OidcFederationProperties())));
    }

    public OidcFederationDefaultTrustChainResolver(final List<TrustChainResolver> trustChainResolvers,
                                                   final OidcFederationTrustMarkValidator trustMarkValidator,
                                                   final OidcFederationResolutionLimiter resolutionLimiter,
                                                   final OidcFederationTrustAnchorKeys trustAnchorKeys) {
        this(trustChainResolvers, trustMarkValidator, resolutionLimiter, trustAnchorKeys,
            new OidcFederationHttpClient(new OidcFederationProperties()));
    }

    /**
     * Build one trust chain resolver per trust anchor. Trust anchors are configured with the keys obtained out of
     * band (OpenID Federation 1.0, section 11.3), or, when their keys are not configured, the keys are taken from their
     * entity configuration. Without any configured trust anchor, the authority hints are used as trust anchors, as
     * before trust anchors could be configured; authority hints are only the immediate superiors of this entity,
     * so chains through an intermediate then end at the intermediate. Every resolver follows at most the configured
     * number of authority hints per entity and the configured path length (section 18.1), and retrieves statements
     * within the request budget of the running resolution.
     *
     * @param properties the federation properties
     * @return the trust chain resolvers
     */
    public static List<TrustChainResolver> buildTrustChainResolvers(final OidcFederationProperties properties) {
        return buildTrustChainResolvers(properties, new OidcFederationBudgetedEntityStatementRetriever(
            new OidcFederationHttpEntityStatementRetriever(new OidcFederationHttpClient(properties))));
    }

    /**
     * Build one trust chain resolver per trust anchor, as {@link #buildTrustChainResolvers(OidcFederationProperties)}
     * does, retrieving entity statements with the given retriever.
     *
     * @param properties the federation properties
     * @param retriever  the entity statement retriever
     * @return the trust chain resolvers
     */
    public static List<TrustChainResolver> buildTrustChainResolvers(final OidcFederationProperties properties,
                                                                    final EntityStatementRetriever retriever) {
        if (properties.getTrustAnchors().isEmpty()) {
            if (properties.getRole() == OidcFederationRole.OPENID_PROVIDER && !properties.getAuthorityHints().isEmpty()) {
                LOGGER.warn("No trust anchors are defined; authority hints [{}] are used as trust anchors with the keys of their own "
                    + "entity configuration. Define trust anchors and their keys with cas.authn.oidc.federation.trust-anchors",
                    properties.getAuthorityHints());
            }
            return properties.getAuthorityHints()
                .stream()
                .map(authorityHint -> buildTrustChainResolver(new EntityID(authorityHint), null, properties, retriever))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return properties.getTrustAnchors()
            .stream()
            .map(Unchecked.function(trustAnchor -> buildTrustChainResolver(new EntityID(trustAnchor.getEntityId()),
                readTrustAnchorKeys(trustAnchor), properties, retriever)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Build the trust chain resolver of a trust anchor, which follows at most the configured number of authority hints
     * per entity and the configured path length.
     *
     * @param trustAnchor the entity identifier of the trust anchor
     * @param keys        the keys the trust anchor is trusted with, or null to take them from its entity configuration
     * @param properties  the federation properties
     * @param retriever   the entity statement retriever
     * @return the trust chain resolver
     */
    public static TrustChainResolver buildTrustChainResolver(final EntityID trustAnchor, @Nullable final JWKSet keys,
                                                             final OidcFederationProperties properties,
                                                             final EntityStatementRetriever retriever) {
        return new TrustChainResolver(Collections.singletonMap(trustAnchor, keys),
            new TrustChainConstraints(properties.getMaxPathLength()),
            new OidcFederationBoundedEntityStatementRetriever(retriever, properties.getMaxAuthorityHints()));
    }

    private static @Nullable JWKSet readTrustAnchorKeys(final OidcFederationTrustAnchorProperties trustAnchor) throws Exception {
        if (StringUtils.isBlank(trustAnchor.getJwks())) {
            LOGGER.warn("Keys of trust anchor [{}] are not defined; they are taken from its own entity configuration", trustAnchor.getEntityId());
            return null;
        }
        val jwks = StringUtils.trim(SpringExpressionLanguageValueResolver.getInstance().resolve(trustAnchor.getJwks()));
        val json = Strings.CS.startsWith(jwks, "{")
            ? jwks
            : IOUtils.toString(ResourceUtils.getRawResourceFrom(jwks).getInputStream(), StandardCharsets.UTF_8);
        val keys = JWKSet.parse(json).toPublicJWKSet();
        LOGGER.debug("Trust anchor [{}] is trusted with keys [{}]", trustAnchor.getEntityId(),
            keys.getKeys().stream().map(JWK::getKeyID).toList());
        return keys;
    }

    /**
     * Resolve the trust chains of the relying party to each trust anchor in turn, as a single resolution within the
     * limits of the resolution limiter, until one gives a relying party that can be registered. When none does and a
     * trust anchor, or what the registration needed, could not be told about, the resolution is unavailable rather than
     * failed.
     *
     * @param id the client id
     * @return the registered service, or empty when no trust chain can be used
     * @throws Exception the exception
     */
    @Override
    public Optional<OidcRegisteredService> resolveTrustChains(final String id) throws Exception {
        if (!Strings.CI.startsWith(id, "https://")) {
            return Optional.empty();
        }
        return resolutionLimiter.execute(() -> {
            val entityId = new EntityID(id);
            val unavailable = new ArrayList<OidcFederationResolutionUnavailableException>();
            for (val trustChainResolver : trustChainResolvers) {
                try {
                    val registeredService = fetchRelyingPartyMetadata(trustChainResolver, entityId)
                        .map(metadata -> toRegisteredService(entityId, metadata));
                    if (registeredService.isPresent()) {
                        return registeredService;
                    }
                } catch (final OidcFederationResolutionUnavailableException e) {
                    unavailable.add(e);
                }
            }
            if (!unavailable.isEmpty()) {
                throw unavailable.getFirst();
            }
            return Optional.<OidcRegisteredService>empty();
        });
    }

    /**
     * Register the relying party from the trust chain it sent, verified as a resolved chain would be: it must start
     * with the entity configuration of the client, end at a configured trust anchor whose keys are defined out of band,
     * verify statement by statement down from those keys, not have expired, and respect the maximum path length; the
     * required trust marks and the metadata policies of the chain then apply as usual. A chain that cannot be used, or
     * a trust anchor trusted with the keys of its own entity configuration, which a sent chain could forge, is refused.
     * Verifying the chain and building the registration from it, which may fetch the sector identifier document of the
     * relying party, is a single resolution within the limits of the resolution limiter.
     *
     * @param id         the client id
     * @param trustChain the serialized statements of the sent trust chain
     * @return the registered service, or empty when the chain cannot be used
     * @throws Exception the exception
     */
    @Override
    public Optional<OidcRegisteredService> resolveSentTrustChain(final String id, final List<String> trustChain) throws Exception {
        if (trustChain.isEmpty() || !Strings.CI.startsWith(id, "https://")) {
            return Optional.empty();
        }
        val entityId = new EntityID(id);
        val registeredService = resolutionLimiter.execute(() -> verifyTrustChain(entityId, trustChain)
            .flatMap(verified -> toRelyingPartyMetadata(verified.getLeft(), verified.getRight()))
            .map(metadata -> toRegisteredService(entityId, metadata)));
        if (registeredService.isEmpty()) {
            LOGGER.debug("Trust chain sent by [{}] cannot be used", entityId);
            return Optional.empty();
        }
        LOGGER.debug("Relying party [{}] is registered from the trust chain it sent", entityId);
        return registeredService;
    }

    /**
     * Verify a trust chain sent by the relying party.
     *
     * @param entityId   the entity id of the relying party
     * @param trustChain the serialized statements
     * @return the trust chain resolver of its trust anchor and the verified chain, or empty when it cannot be used
     */
    protected Optional<Pair<TrustChainResolver, TrustChain>> verifyTrustChain(final EntityID entityId, final List<String> trustChain) {
        try {
            val chain = TrustChain.parseSerialized(trustChain);
            if (!entityId.equals(chain.getLeafConfiguration().getEntityID())) {
                LOGGER.warn("Trust chain sent by [{}] starts at [{}]", entityId, chain.getLeafConfiguration().getEntityID());
                return Optional.empty();
            }
            val trustAnchorId = chain.getTrustAnchorEntityID();
            val trustChainResolver = trustChainResolvers
                .stream()
                .filter(resolver -> resolver.getTrustAnchors().get(trustAnchorId) != null)
                .findFirst();
            if (trustChainResolver.isEmpty()) {
                LOGGER.warn("Trust chain sent by [{}] ends at [{}], which is not a trust anchor with keys defined out of band",
                    entityId, trustAnchorId);
                return Optional.empty();
            }
            chain.verifySignatures(trustChainResolver.get().getTrustAnchors().get(trustAnchorId));
            if (!chain.resolveExpirationTime().after(new Date())) {
                LOGGER.warn("Trust chain sent by [{}] has expired", entityId);
                return Optional.empty();
            }
            val chains = new TrustChainSet();
            chains.add(chain);
            if (chains.filter(trustChainResolver.get().getConstraints()).isEmpty() || !satisfiesConstraints(chain)) {
                LOGGER.warn("Trust chain sent by [{}] is longer than allowed or breaks the constraints of its statements", entityId);
                return Optional.empty();
            }
            return Optional.of(Pair.of(trustChainResolver.get(), chain));
        } catch (final Exception e) {
            LOGGER.warn("Trust chain sent by [{}] cannot be verified: [{}]", entityId, e.getMessage());
            return Optional.empty();
        }
    }

    private @Nullable OidcRegisteredService toRegisteredService(final EntityID entityId, final TrustChainMetadata trustMetadata) {
        val registeredService = extractRegisteredService(entityId, trustMetadata.metadata(), trustMetadata.expirationTime());
        if (registeredService != null) {
            registeredService.getProperties().put(OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT,
                new DefaultRegisteredServiceProperty(String.valueOf(trustMetadata.issuedAt().toInstant().getEpochSecond())));
        }
        return registeredService;
    }

    /**
     * Build the temporary service of a relying party from its resolved metadata. The relying party must authenticate
     * with {@code private_key_jwt}, which is assumed when its metadata names no method, or {@code tls_client_auth};
     * any other method is refused, and so is a relying party without redirect URIs. Post-logout redirect URIs are kept
     * as a comma-separated list, so one that contains a comma is left out: it would otherwise be split into URIs that
     * the metadata policies of the chain never saw. Keys published at a {@code jwks_uri} must be served over
     * {@code https}, so that a relying party cannot have CAS read keys from local files or other schemes. A relying party
     * that asks for pairwise subject identifiers must have a sector of its own, as {@link #hasVerifiedSector} tells.
     *
     * @param entityId       the entity id
     * @param rpMetadata     the resolved relying party metadata
     * @param expirationTime the expiration time of the trust chain
     * @return the registered service, or null when the relying party cannot be registered automatically
     */
    protected @Nullable OidcRegisteredService extractRegisteredService(final EntityID entityId, final OIDCClientMetadata rpMetadata,
                                                                       final Date expirationTime) {
        val authenticationMethod = Objects.requireNonNullElse(rpMetadata.getTokenEndpointAuthMethod(), ClientAuthenticationMethod.PRIVATE_KEY_JWT);
        if (!ASYMMETRIC_CLIENT_AUTHENTICATION_METHODS.contains(authenticationMethod)) {
            LOGGER.warn("Relying party [{}] cannot be registered automatically with client authentication method [{}]; "
                + "use one of [{}]", entityId, authenticationMethod, ASYMMETRIC_CLIENT_AUTHENTICATION_METHODS);
            return null;
        }
        if (rpMetadata.getRedirectionURIStrings() == null || rpMetadata.getRedirectionURIStrings().isEmpty()) {
            LOGGER.warn("Relying party [{}] cannot be registered automatically without redirect URIs", entityId);
            return null;
        }
        val jwksUri = rpMetadata.getJWKSetURI();
        if (jwksUri != null && !Strings.CI.equals(jwksUri.getScheme(), "https")) {
            LOGGER.warn("Relying party [{}] cannot be registered automatically with keys published at [{}], which is not an https URL",
                entityId, jwksUri);
            return null;
        }
        if (!hasVerifiedSector(entityId, rpMetadata)) {
            return null;
        }
        val registeredService = new OidcRegisteredService();
        registeredService.assignIdIfNecessary();

        registeredService.setTlsClientAuthSanDns(rpMetadata.getTLSClientAuthSanDNS());
        registeredService.setTlsClientAuthSanEmail(rpMetadata.getTLSClientAuthSanEmail());
        registeredService.setTlsClientAuthSanIp(rpMetadata.getTLSClientAuthSanIP());
        registeredService.setTlsClientAuthSanUri(rpMetadata.getTLSClientAuthSanURI());
        registeredService.setTlsClientAuthSubjectDn(rpMetadata.getTLSClientAuthSubjectDN());

        registeredService.setName(rpMetadata.getName());
        registeredService.setDescription(rpMetadata.getOrganizationName());
        registeredService.setClientId(entityId.toClientID().getValue());

        registeredService.setScopes(new HashSet<>(rpMetadata.getScope().toStringList()));
        registeredService.setApplicationType(Objects.requireNonNullElse(rpMetadata.getApplicationType(), ApplicationType.WEB).toString());
        val subjectType = rpMetadata.getSubjectType();
        if (subjectType != null) {
            registeredService.setSubjectType(subjectType.toString());
        }

        FunctionUtils.doIfNotNull(rpMetadata.getSectorIDURI(), value -> registeredService.setSectorIdentifierUri(value.toASCIIString()));

        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWSAlg(), value -> registeredService.setIdTokenSigningAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWEAlg(), value -> registeredService.setIdTokenEncryptionAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getIDTokenJWEEnc(), value -> registeredService.setIdTokenEncryptionEncoding(value.getName()));

        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWSAlg(), value -> registeredService.setUserInfoSigningAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWEAlg(), value -> registeredService.setUserInfoEncryptedResponseAlg(value.getName()));
        FunctionUtils.doIfNotNull(rpMetadata.getUserInfoJWEEnc(), value -> registeredService.setUserInfoEncryptedResponseEncoding(value.getName()));

        if (rpMetadata.getEmailContacts() != null) {
            val contacts = rpMetadata.getEmailContacts()
                .stream()
                .map(email -> new DefaultRegisteredServiceContact().setEmail(email))
                .collect(Collectors.<RegisteredServiceContact>toList());
            registeredService.setContacts(contacts);
        }

        registeredService.setServiceId(OidcClientRegistrationUtils.toServiceId(new TreeSet<>(rpMetadata.getRedirectionURIStrings())));

        FunctionUtils.doIfNotNull(rpMetadata.getLogoURI(), value -> registeredService.setLogo(value.toURL().toExternalForm()));
        FunctionUtils.doIfNotNull(rpMetadata.getPolicyURI(), value -> registeredService.setPrivacyUrl(value.toURL().toExternalForm()));
        FunctionUtils.doIfNotNull(rpMetadata.getTermsOfServiceURI(), value -> registeredService.setInformationUrl(value.toURL().toExternalForm()));

        if (rpMetadata.getGrantTypes() != null) {
            val grantTypes = rpMetadata.getGrantTypes().stream().map(GrantType::toString).collect(Collectors.toSet());
            registeredService.setSupportedGrantTypes(grantTypes);
        }

        if (rpMetadata.getResponseTypes() != null) {
            val responseTypes = rpMetadata.getResponseTypes().stream().map(ResponseType::toString).collect(Collectors.toSet());
            registeredService.setSupportedResponseTypes(responseTypes);
        }

        if (rpMetadata.getPostLogoutRedirectionURIs() != null) {
            val logoutUrls = rpMetadata.getPostLogoutRedirectionURIs()
                .stream()
                .map(Unchecked.function(url -> url.toURL().toExternalForm()))
                .filter(url -> {
                    val separated = Strings.CS.contains(url, ",");
                    if (separated) {
                        LOGGER.warn("Post-logout redirect URI [{}] of [{}] contains a comma and is left out", url, entityId);
                    }
                    return !separated;
                })
                .collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(logoutUrls)) {
                registeredService.setLogoutUrl(logoutUrls);
            }
        }

        FunctionUtils.doIfNotNull(rpMetadata.getJWKSetURI(),
            value -> registeredService.setJwks(value.toURL().toExternalForm()));
        if (rpMetadata.getJWKSetURI() == null && rpMetadata.getJWKSet() != null && !rpMetadata.getJWKSet().getKeys().isEmpty()) {
            val jwks = rpMetadata.getJWKSet();
            registeredService.setJwks(jwks.toString());
        }
        registeredService.setTokenEndpointAuthenticationMethod(authenticationMethod.getValue());

        registeredService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, expirationTime.toInstant().atZone(ZoneOffset.UTC)));

        registeredService.getProperties().put(TEMPORARY_OPENIDFEDERATION_SERVICE, new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));

        return registeredService;
    }

    /**
     * Whether the sector of a relying party that asks for pairwise subject identifiers is its own, since relying parties
     * of the same sector share identifiers (OpenID Connect Core 1.0, section 8.1). Its {@code sector_identifier_uri} must
     * be fetched, as a request of the running resolution, and list every redirect URI of the relying party; without one,
     * its redirect URIs must all be in the same sector, read as sectors are when identifiers are calculated: a web
     * redirect URI is in the sector of its host and any other one in the relying party's own, and a web redirect URI
     * whose host cannot be read is refused. A sector identifier URI must be an {@code https} URL
     * that federation requests may be sent to. A relying party that does not ask for pairwise subject identifiers has
     * nothing to verify. A sector identifier document that cannot be had now, because the request fails,
     * the server answers with an error of its own, or the resolution runs out of time, leaves the sector unknown rather
     * than refused, so that the relying party is not remembered as unresolved for an outage; a resolution that used up
     * its requests refuses it.
     *
     * @param entityId   the entity identifier of the relying party
     * @param rpMetadata the metadata of the relying party
     * @return true or false
     * @throws OidcFederationResolutionUnavailableException when the sector identifier document cannot be had now
     */
    protected boolean hasVerifiedSector(final EntityID entityId, final OIDCClientMetadata rpMetadata) {
        if (!SubjectType.PAIRWISE.equals(rpMetadata.getSubjectType())) {
            return true;
        }
        val redirectUris = rpMetadata.getRedirectionURIStrings();
        val sectorUri = rpMetadata.getSectorIDURI();
        if (sectorUri == null) {
            try {
                val sectors = OidcRedirectUriUtils.toSectors(redirectUris);
                if (sectors.size() > 1) {
                    LOGGER.warn("Relying party [{}] asks for pairwise subject identifiers for redirect URIs in several sectors [{}] without a "
                        + "sector identifier URI", entityId, sectors);
                }
                return sectors.size() <= 1;
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("Relying party [{}] asks for pairwise subject identifiers: [{}]", entityId, e.getMessage());
                return false;
            }
        }
        if (!Strings.CI.equals(sectorUri.getScheme(), "https")
            || OidcRedirectUriUtils.toHost(sectorUri.toString()).filter(host -> host.equals(sectorUri.getHost())).isEmpty()) {
            LOGGER.warn("Sector identifier URI [{}] of relying party [{}] is not an https URL with a host that can be read", sectorUri, entityId);
            return false;
        }
        val response = fetchSectorIdentifierDocument(entityId, sectorUri);
        if (response == null) {
            return false;
        }
        if (response.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            LOGGER.warn("Sector identifier URI [{}] of relying party [{}] answers [{}]", sectorUri, entityId, response.getStatusCode());
            throw new OidcFederationResolutionUnavailableException("Sector identifier URI %s cannot be read now".formatted(sectorUri));
        }
        try {
            val listed = response.indicatesSuccess() ? JSONArrayUtils.parse(response.getBody()) : List.of();
            if (listed.containsAll(redirectUris)) {
                return true;
            }
            LOGGER.warn("Sector identifier URI [{}] of relying party [{}] does not list all of its redirect URIs", sectorUri, entityId);
        } catch (final Exception e) {
            LOGGER.warn("Sector identifier document [{}] of relying party [{}] cannot be read: [{}]", sectorUri, entityId, e.getMessage());
        }
        return false;
    }

    private @Nullable HTTPResponse fetchSectorIdentifierDocument(final EntityID entityId, final URI sectorUri) {
        try {
            OidcFederationResolutionLimiter.consumeRequest("sector identifier document " + sectorUri);
            return httpClient.send(new HTTPRequest(HTTPRequest.Method.GET, sectorUri));
        } catch (final OidcFederationHttpClient.RefusedRequestException | IllegalStateException e) {
            LOGGER.warn("Sector identifier URI [{}] of relying party [{}] may not be requested: [{}]", sectorUri, entityId, e.getMessage());
            return null;
        } catch (final IOException e) {
            LOGGER.warn("Sector identifier URI [{}] of relying party [{}] cannot be read now: [{}]", sectorUri, entityId, e.getMessage());
            throw new OidcFederationResolutionUnavailableException("Sector identifier URI %s cannot be read now".formatted(sectorUri));
        }
    }

    /**
     * The relying party metadata of the shortest trust chain of the relying party to the trust anchor of the resolver
     * that satisfies the constraints of its statements.
     *
     * @param trustChainResolver the trust chain resolver of the trust anchor
     * @param entityId           the entity identifier of the relying party
     * @return the metadata, or empty when no trust chain can be used
     * @throws OidcFederationResolutionUnavailableException when the trust anchor could not be reached, or its historical
     *                                                      keys could not be read, or no trust chain was found while a
     *                                                      request ran out of time or found no room, so that the chains
     *                                                      cannot be told
     */
    protected Optional<TrustChainMetadata> fetchRelyingPartyMetadata(final TrustChainResolver trustChainResolver,
                                                                     final EntityID entityId) {
        val mark = OidcFederationResolutionLimiter.indeterminateMark();
        val unreachableMarks = trustChainResolver.getTrustAnchors().keySet().stream()
            .collect(Collectors.toMap(Function.identity(), OidcFederationResolutionLimiter::unreachableMark));
        return FunctionUtils.doAndHandle(() -> {
            val validChains = new TrustChainSet();
            trustChainResolver.resolveTrustChains(entityId).stream().filter(this::satisfiesConstraints).forEach(validChains::add);
            if (validChains.isEmpty()) {
                if (OidcFederationResolutionLimiter.isIndeterminateSince(mark)) {
                    throw new OidcFederationResolutionUnavailableException("Trust chains of %s cannot be resolved now".formatted(entityId));
                }
                LOGGER.warn("No trust chain of [{}] satisfies the constraints of its statements", entityId);
                return Optional.<TrustChainMetadata>empty();
            }
            return toRelyingPartyMetadata(trustChainResolver, validChains.getShortest());
        }, e -> {
            if (e instanceof final OidcFederationResolutionUnavailableException unavailable) {
                throw unavailable;
            }
            val unreachable = unreachableMarks.entrySet().stream()
                .filter(entry -> OidcFederationResolutionLimiter.isUnreachableSince(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();
            if (unreachable.isPresent()) {
                throw new OidcFederationResolutionUnavailableException("Trust anchor %s cannot be reached now".formatted(unreachable.get()));
            }
            if (OidcFederationResolutionLimiter.isIndeterminateSince(mark)) {
                throw new OidcFederationResolutionUnavailableException("Trust chains of %s cannot be resolved now".formatted(entityId));
            }
            return Optional.<TrustChainMetadata>empty();
        }).get();
    }

    /**
     * Whether the trust chain of a relying party satisfies the {@code constraints} of each of its subordinate statements,
     * whose allowed entity types must then include {@code openid_relying_party}.
     *
     * @param chain the trust chain
     * @return true or false
     */
    protected boolean satisfiesConstraints(final TrustChain chain) {
        return OidcFederationTrustChainConstraints.isSatisfiedBy(chain, EntityType.OPENID_RELYING_PARTY);
    }

    /**
     * The relying party metadata of a verified trust chain, resolved with the metadata its immediate superior asserts and
     * the metadata policies of the chain, once the relying party holds the required trust marks.
     *
     * @param trustChainResolver the trust chain resolver of the trust anchor of the chain
     * @param chain              the trust chain
     * @return the metadata, or empty when the relying party lacks a required trust mark or its metadata is invalid
     */
    protected Optional<TrustChainMetadata> toRelyingPartyMetadata(final TrustChainResolver trustChainResolver, final TrustChain chain) {
        return FunctionUtils.doAndHandle(() -> {
            if (isSignedWithRevokedTrustAnchorKey(trustChainResolver, chain)
                || !trustMarkValidator.hasRequiredTrustMarks(trustChainResolver, chain)) {
                return Optional.<TrustChainMetadata>empty();
            }
            val clientMetadataJson = OidcFederationMetadataPolicies.resolveMetadata(chain, EntityType.OPENID_RELYING_PARTY);
            return Optional.of(new TrustChainMetadata(OIDCClientMetadata.parse(clientMetadataJson),
                chain.resolveExpirationTime(), chain.getLeafConfiguration().getClaimsSet().getIssueTime()));
        }, e -> {
            if (e instanceof final OidcFederationResolutionUnavailableException unavailable) {
                throw unavailable;
            }
            return Optional.<TrustChainMetadata>empty();
        }).get();
    }

    /**
     * Whether the trust anchor signed its statements in the chain with a key it has since revoked.
     *
     * @param trustChainResolver the trust chain resolver of the trust anchor of the chain
     * @param chain              the trust chain
     * @return true or false
     * @see OidcFederationTrustAnchorKeys#isSignedWithRevokedKey(TrustChainResolver, TrustChain)
     */
    protected boolean isSignedWithRevokedTrustAnchorKey(final TrustChainResolver trustChainResolver, final TrustChain chain) {
        return trustAnchorKeys.isSignedWithRevokedKey(trustChainResolver, chain);
    }

    protected record TrustChainMetadata(OIDCClientMetadata metadata, Date expirationTime, Date issuedAt) {
    }
}
