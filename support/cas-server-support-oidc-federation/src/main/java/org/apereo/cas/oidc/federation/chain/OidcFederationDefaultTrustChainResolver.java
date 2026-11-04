package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustAnchorProperties;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkValidator;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceContact;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.federation.entities.CommonFederationClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.DefaultEntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainSet;
import com.nimbusds.openid.connect.sdk.federation.trust.constraints.TrustChainConstraints;
import com.nimbusds.openid.connect.sdk.rp.ApplicationType;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
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

    private static final String HISTORICAL_KEYS_ENDPOINT = "federation_historical_keys_endpoint";

    private static final JOSEObjectType HISTORICAL_KEYS_TYPE = new JOSEObjectType("jwk-set+jwt");

    private static final String KEYS = "keys";

    private static final int HISTORICAL_KEYS_TIMEOUT_MILLIS = 5000;

    /**
     * Client authentication methods an automatically registered client may use: asymmetric cryptography is required,
     * since no secret is ever issued to it (OpenID Federation 1.0, section 12.1).
     */
    private static final Set<ClientAuthenticationMethod> ASYMMETRIC_CLIENT_AUTHENTICATION_METHODS =
        Set.of(ClientAuthenticationMethod.PRIVATE_KEY_JWT, ClientAuthenticationMethod.TLS_CLIENT_AUTH);

    private final List<TrustChainResolver> trustChainResolvers;

    private final OidcFederationTrustMarkValidator trustMarkValidator;

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
                    return Optional.ofNullable(toRegisteredService(entityId, trustMetadataResult.get()));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Register the relying party from the trust chain it sent, verified as a resolved chain would be: it must start
     * with the entity configuration of the client, end at a configured trust anchor whose keys are defined out of band,
     * verify statement by statement down from those keys, not have expired, and respect the maximum path length; the
     * required trust marks and the metadata policies of the chain then apply as usual. A chain that cannot be used, or
     * a trust anchor trusted with the keys of its own entity configuration, which a sent chain could forge, has the
     * trust chains resolved instead.
     *
     * @param id         the client id
     * @param trustChain the serialized statements of the sent trust chain
     * @return the registered service
     * @throws Exception the exception
     */
    @Override
    public Optional<OidcRegisteredService> resolveTrustChains(final String id, final List<String> trustChain) throws Exception {
        if (!trustChain.isEmpty() && Strings.CI.startsWith(id, "https://")) {
            val entityId = new EntityID(id);
            val sentMetadata = verifyTrustChain(entityId, trustChain)
                .flatMap(verified -> toRelyingPartyMetadata(verified.getLeft(), verified.getRight()));
            if (sentMetadata.isPresent()) {
                LOGGER.debug("Relying party [{}] is registered from the trust chain it sent", entityId);
                return Optional.ofNullable(toRegisteredService(entityId, sentMetadata.get()));
            }
            LOGGER.debug("Trust chain sent by [{}] cannot be used; its trust chains are resolved instead", entityId);
        }
        return resolveTrustChains(id);
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
            val validChains = new TrustChainSet();
            trustChainResolver.resolveTrustChains(entityId).stream().filter(this::satisfiesConstraints).forEach(validChains::add);
            if (validChains.isEmpty()) {
                LOGGER.warn("No trust chain of [{}] satisfies the constraints of its statements", entityId);
                return Optional.<TrustChainMetadata>empty();
            }
            return toRelyingPartyMetadata(trustChainResolver, validChains.getShortest());
        }, e -> Optional.<TrustChainMetadata>empty()).get();
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
     * The relying party metadata of a verified trust chain, with the metadata policies of the chain applied, once the
     * relying party holds the required trust marks.
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
            val metadataPolicy = chain.resolveCombinedMetadataPolicy(EntityType.OPENID_RELYING_PARTY);
            val claims = chain.getLeafConfiguration().getSignedStatement().getJWTClaimsSet();
            val metadata = (Map) claims.getClaim(CommonFederationClaimsSet.METADATA_CLAIM_NAME);
            val rawRp = new JSONObject((Map) metadata.get(EntityType.OPENID_RELYING_PARTY.getValue()));
            val clientMetadataJson = metadataPolicy.apply(rawRp);
            return Optional.of(new TrustChainMetadata(OIDCClientMetadata.parse(clientMetadataJson),
                chain.resolveExpirationTime(), chain.getLeafConfiguration().getClaimsSet().getIssueTime()));
        }, e -> Optional.<TrustChainMetadata>empty()).get();
    }

    /**
     * Whether the trust anchor signed its statements in the chain with a key it has since revoked (OpenID Federation 1.0,
     * section 8.7), as published by its federation historical keys endpoint. The keys a trust anchor is trusted with
     * out of band may still list a key it has revoked; its historical keys are verified with those keys, and the
     * entity configuration that advertises them comes from the chain or is fetched. When the trust anchor publishes no
     * historical keys, or they cannot be obtained or verified, the chain is not refused on that account, which is
     * reported in the logs.
     *
     * @param trustChainResolver the trust chain resolver of the trust anchor of the chain
     * @param chain              the trust chain
     * @return true or false
     */
    protected boolean isSignedWithRevokedTrustAnchorKey(final TrustChainResolver trustChainResolver, final TrustChain chain) {
        val trustAnchorId = chain.getTrustAnchorEntityID();
        try {
            val trustAnchorKeys = trustChainResolver.getTrustAnchors().get(trustAnchorId);
            val configuration = Objects.requireNonNullElseGet(chain.getTrustAnchorConfiguration(),
                Unchecked.supplier(() -> trustChainResolver.getEntityStatementRetriever().fetchEntityConfiguration(trustAnchorId)));
            if (trustAnchorKeys == null) {
                configuration.verifySignatureOfSelfStatement();
            } else {
                configuration.verifySignature(trustAnchorKeys);
            }
            val federationEntity = configuration.getClaimsSet().getMetadata(EntityType.FEDERATION_ENTITY);
            val endpoint = federationEntity == null ? null : federationEntity.get(HISTORICAL_KEYS_ENDPOINT);
            if (endpoint == null) {
                return false;
            }
            val revokedKeys = fetchRevokedKeys(endpoint.toString(), trustAnchorId,
                Objects.requireNonNullElseGet(trustAnchorKeys, () -> configuration.getClaimsSet().getJWKSet()));
            val statements = new ArrayList<EntityStatement>();
            statements.add(chain.getSuperiorStatements().getLast());
            FunctionUtils.doIfNotNull(chain.getTrustAnchorConfiguration(), statements::add);
            val revoked = statements.stream()
                .map(statement -> statement.getSignedStatement().getHeader().getKeyID())
                .filter(Objects::nonNull)
                .anyMatch(revokedKeys::contains);
            if (revoked) {
                LOGGER.warn("Trust chain of [{}] is signed with a key that trust anchor [{}] has revoked",
                    chain.getLeafConfiguration().getEntityID(), trustAnchorId);
            }
            return revoked;
        } catch (final Exception e) {
            LOGGER.warn("Unable to check the revoked keys of trust anchor [{}]: [{}]", trustAnchorId, e.getMessage());
            return false;
        }
    }

    private static Set<String> fetchRevokedKeys(final String endpoint, final EntityID trustAnchorId, final JWKSet trustAnchorKeys)
            throws Exception {
        val request = new HTTPRequest(HTTPRequest.Method.GET, new URI(endpoint));
        request.setConnectTimeout(HISTORICAL_KEYS_TIMEOUT_MILLIS);
        request.setReadTimeout(HISTORICAL_KEYS_TIMEOUT_MILLIS);
        val response = request.send();
        if (!response.indicatesSuccess()) {
            throw new IllegalStateException("Historical keys endpoint " + endpoint + " answered " + response.getStatusCode());
        }
        val processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(HISTORICAL_KEYS_TYPE));
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(Set.copyOf(JWSAlgorithm.Family.SIGNATURE),
            new ImmutableJWKSet<>(trustAnchorKeys)));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder().issuer(trustAnchorId.getValue()).build(), Set.of(JWTClaimNames.ISSUER, JWTClaimNames.ISSUED_AT, KEYS)));
        val claims = processor.process(response.getBody(), null);
        val now = new Date();
        return JWKSet.parse(Map.<String, Object>of(KEYS, claims.getListClaim(KEYS)))
            .getKeys()
            .stream()
            .filter(key -> key.getKeyRevocation() != null && !key.getKeyRevocation().getRevocationTime().after(now))
            .map(JWK::getKeyID)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    protected record TrustChainMetadata(OIDCClientMetadata metadata, Date expirationTime, Date issuedAt) {
    }
}
