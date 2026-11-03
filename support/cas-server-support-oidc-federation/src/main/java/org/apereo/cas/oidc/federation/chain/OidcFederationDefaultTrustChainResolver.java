package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
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
import com.nimbusds.openid.connect.sdk.federation.entities.CommonFederationClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.DefaultEntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.federation.trust.constraints.TrustChainConstraints;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
     * Client authentication methods an automatically registered client may use: asymmetric cryptography is required,
     * since no secret is ever issued to it (OpenID Federation 1.0, section 12.1).
     */
    private static final Set<ClientAuthenticationMethod> ASYMMETRIC_CLIENT_AUTHENTICATION_METHODS =
        Set.of(ClientAuthenticationMethod.PRIVATE_KEY_JWT, ClientAuthenticationMethod.TLS_CLIENT_AUTH);

    private final List<TrustChainResolver> trustChainResolvers;

    /**
     * Build one trust chain resolver per trust anchor. Trust anchors are configured with the keys obtained out of
     * band (OpenID Federation 1.0, section 11.3), or, when their keys are not configured, the keys are taken from their
     * entity configuration. Without any configured trust anchor, the authority hints are used as trust anchors, as
     * before trust anchors could be configured; authority hints are only the immediate superiors of this entity,
     * so chains through an intermediate then end at the intermediate. Every resolver follows at most the configured
     * number of authority hints per entity and the configured path length (section 18.1).
     *
     * @param properties the federation properties
     * @return the trust chain resolvers
     */
    public static List<TrustChainResolver> buildTrustChainResolvers(final OidcFederationProperties properties) {
        if (properties.getTrustAnchors().isEmpty()) {
            if (properties.getRole() == OidcFederationRole.OPENID_PROVIDER && !properties.getAuthorityHints().isEmpty()) {
                LOGGER.warn("No trust anchors are defined; authority hints [{}] are used as trust anchors with the keys of their own "
                    + "entity configuration. Define trust anchors and their keys with cas.authn.oidc.federation.trust-anchors",
                    properties.getAuthorityHints());
            }
            return properties.getAuthorityHints()
                .stream()
                .map(authorityHint -> buildTrustChainResolver(new EntityID(authorityHint), null, properties))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return properties.getTrustAnchors()
            .stream()
            .map(Unchecked.function(trustAnchor -> buildTrustChainResolver(new EntityID(trustAnchor.getEntityId()),
                readTrustAnchorKeys(trustAnchor), properties)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static TrustChainResolver buildTrustChainResolver(final EntityID trustAnchor, @Nullable final JWKSet keys,
                                                              final OidcFederationProperties properties) {
        val retriever = new OidcFederationBoundedEntityStatementRetriever(new DefaultEntityStatementRetriever(),
            properties.getMaxAuthorityHints());
        return new TrustChainResolver(Collections.singletonMap(trustAnchor, keys),
            new TrustChainConstraints(properties.getMaxPathLength()), retriever);
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

    @Override
    public Optional<OidcRegisteredService> resolveTrustChains(final String id) throws Exception {
        if (Strings.CI.startsWith(id, "https://")) {
            val entityId = new EntityID(id);
            for (val trustChainResolver : trustChainResolvers) {
                val trustMetadataResult = fetchRelyingPartyMetadata(trustChainResolver, entityId);
                if (trustMetadataResult.isPresent()) {
                    val trustMetadata = trustMetadataResult.get();
                    val oidcRegisteredService = extractRegisteredService(entityId, trustMetadata.metadata(), trustMetadata.expirationTime());
                    return Optional.ofNullable(oidcRegisteredService);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Build the temporary service of a relying party from its resolved metadata. The relying party must authenticate
     * with {@code private_key_jwt}, which is assumed when its metadata names no method, or {@code tls_client_auth};
     * any other method is refused.
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
        registeredService.setApplicationType(rpMetadata.getApplicationType().toString());
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

        if (rpMetadata.getRedirectionURIStrings() != null) {
            val redirectUris = rpMetadata.getRedirectionURIStrings()
                    .stream()
                    .map(uri -> uri.replace(".", "\\.").replace("?", "\\?"))
                    .collect(Collectors.joining(","));
            registeredService.setServiceId(redirectUris);
        }

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
                .collect(Collectors.joining(","));
            registeredService.setLogoutUrl(logoutUrls);
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

    protected Optional<TrustChainMetadata> fetchRelyingPartyMetadata(final TrustChainResolver trustChainResolver,
                                                                     final EntityID entityId) {
        return FunctionUtils.doAndHandle(() -> {
            val resolvedChains = trustChainResolver.resolveTrustChains(entityId);
            val chain = resolvedChains.getShortest();
            val metadataPolicy = chain.resolveCombinedMetadataPolicy(EntityType.OPENID_RELYING_PARTY);

            val claims = chain.getLeafConfiguration().getSignedStatement().getJWTClaimsSet();
            val metadata = (Map) claims.getClaim(CommonFederationClaimsSet.METADATA_CLAIM_NAME);
            val rawRp = new JSONObject((Map) metadata.get(EntityType.OPENID_RELYING_PARTY.getValue()));
            val clientMetadataJson = metadataPolicy.apply(rawRp);
            return Optional.of(new TrustChainMetadata(OIDCClientMetadata.parse(clientMetadataJson), chain.resolveExpirationTime()));
        }, e -> Optional.<TrustChainMetadata>empty()).get();
    }

    protected record TrustChainMetadata(OIDCClientMetadata metadata, Date expirationTime) {
    }
}
