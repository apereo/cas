package org.apereo.cas.oidc.federation.trustmark;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.oidc.federation.chain.OidcFederationHistoricalKeys;
import org.apereo.cas.oidc.federation.chain.OidcFederationHttpClient;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionLimiter;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainConstraints;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.common.contenttype.ContentType;
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
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.util.URLUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcFederationDefaultTrustMarkValidator}. A trust mark is valid when it is a signed JWT typed
 * {@code trust-mark+jwt} that names its signing key, is about the entity, is of the required type, was issued and
 * has not expired; when the trust anchor of the entity's trust chain accepts its issuer for that type in
 * {@code trust_mark_issuers}; when it verifies with the issuer's federation keys, taken from the trust anchor itself
 * or from the issuer's own trust chain to the same trust anchor, or with a key the issuer has retired since, as its
 * historical keys publish it, when the key was never revoked and the trust mark was issued before it was retired; when it carries a valid {@code delegation} for a type
 * that the trust anchor lists in {@code trust_mark_owners}; and, when the issuer publishes a trust mark status
 * endpoint, when that endpoint reports it {@code active} (OpenID Federation 1.0, sections 3.1, 7 and 8.5).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationDefaultTrustMarkValidator implements OidcFederationTrustMarkValidator {
    /**
     * Type of trust marks.
     */
    public static final JOSEObjectType TRUST_MARK_JWT_TYPE = new JOSEObjectType("trust-mark+jwt");

    /**
     * Type of trust mark delegations.
     */
    public static final JOSEObjectType TRUST_MARK_DELEGATION_JWT_TYPE = new JOSEObjectType("trust-mark-delegation+jwt");

    /**
     * Type of trust mark status responses.
     */
    public static final JOSEObjectType TRUST_MARK_STATUS_RESPONSE_JWT_TYPE = new JOSEObjectType("trust-mark-status-response+jwt");

    private static final String TRUST_MARKS = "trust_marks";

    private static final String TRUST_MARK = "trust_mark";

    private static final String TRUST_MARK_TYPE = "trust_mark_type";

    private static final String TRUST_MARK_ISSUERS = "trust_mark_issuers";

    private static final String TRUST_MARK_OWNERS = "trust_mark_owners";

    private static final String DELEGATION = "delegation";

    private static final String STATUS = "status";

    private static final String STATUS_ACTIVE = "active";

    private static final String STATUS_ENDPOINT = "federation_trust_mark_status_endpoint";

    private static final String METADATA = "metadata";

    private static final int MAX_CLOCK_SKEW_SECONDS = 60;

    private static final Duration HISTORICAL_KEYS_CACHE_DURATION = Duration.ofMinutes(1);

    private static final int HISTORICAL_KEYS_CACHE_SIZE = 1000;

    private final OidcFederationProperties properties;

    private final OidcFederationHttpClient httpClient;

    /**
     * The historical keys of trust mark issuers, by issuer, endpoint and verification keys, including issuers whose
     * historical keys could not be obtained, so that trust marks naming keys their issuer does not publish cannot make CAS
     * request the same historical keys over and over.
     */
    private final Cache<String, List<JWK>> historicalKeys = Caffeine.newBuilder()
        .maximumSize(HISTORICAL_KEYS_CACHE_SIZE)
        .expireAfterWrite(HISTORICAL_KEYS_CACHE_DURATION)
        .build();

    public OidcFederationDefaultTrustMarkValidator(final OidcFederationProperties properties) {
        this(properties, new OidcFederationHttpClient(properties));
    }

    @Override
    public boolean hasRequiredTrustMarks(final TrustChainResolver trustChainResolver, final TrustChain trustChain) {
        val requiredTypes = properties.getRequiredTrustMarks();
        if (requiredTypes.isEmpty()) {
            return true;
        }
        val entityId = trustChain.getLeafConfiguration().getEntityID();
        val mark = StepMark.of(trustChain.getTrustAnchorEntityID());
        try {
            val trustAnchor = resolveTrustAnchorConfiguration(trustChainResolver, trustChain);
            val trustMarks = readTrustMarks(trustChain.getLeafConfiguration());
            var unavailable = Optional.<OidcFederationResolutionUnavailableException>empty();
            for (val type : requiredTypes) {
                val candidates = trustMarks
                    .stream()
                    .filter(entry -> type.equals(entry.get(TRUST_MARK_TYPE)))
                    .map(entry -> entry.get(TRUST_MARK))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
                try {
                    if (!hasValidTrustMark(candidates, type, entityId, trustChainResolver, trustAnchor)) {
                        LOGGER.warn("[{}] holds no valid trust mark of type [{}], which is required of relying parties", entityId, type);
                        return false;
                    }
                } catch (final OidcFederationResolutionUnavailableException e) {
                    unavailable = Optional.of(e);
                }
            }
            if (unavailable.isPresent()) {
                throw unavailable.get();
            }
            return true;
        } catch (final OidcFederationResolutionUnavailableException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unable to validate the trust marks of [{}]: [{}]", entityId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throwIfCannotTell(mark, entityId, trustChain.getTrustAnchorEntityID());
            return false;
        }
    }

    /**
     * Whether one of the trust marks of the type is valid. A trust mark whose validity cannot be told now, for want of time
     * or room among the running requests, makes the answer unavailable only when no other one of the type is valid.
     *
     * @param candidates         the trust marks of the type
     * @param type               the trust mark type
     * @param entityId           the entity the trust marks must be about
     * @param trustChainResolver the trust chain resolver
     * @param trustAnchor        the entity configuration of the trust anchor
     * @return true or false
     */
    private boolean hasValidTrustMark(final List<String> candidates, final String type, final EntityID entityId,
                                      final TrustChainResolver trustChainResolver, final EntityStatement trustAnchor) {
        var unavailable = Optional.<OidcFederationResolutionUnavailableException>empty();
        for (val trustMark : candidates) {
            try {
                if (isValidTrustMark(trustMark, type, entityId, trustChainResolver, trustAnchor)) {
                    return true;
                }
            } catch (final OidcFederationResolutionUnavailableException e) {
                unavailable = Optional.of(e);
            }
        }
        if (unavailable.isPresent()) {
            throw unavailable.get();
        }
        return false;
    }

    @Override
    public ValidTrustMarks resolveValidTrustMarks(final TrustChainResolver trustChainResolver, final TrustChain trustChain) {
        val entityId = trustChain.getLeafConfiguration().getEntityID();
        val mark = StepMark.of(trustChain.getTrustAnchorEntityID());
        try {
            val trustAnchor = resolveTrustAnchorConfiguration(trustChainResolver, trustChain);
            val valid = new ArrayList<Map<String, Object>>();
            var complete = true;
            for (val entry : readTrustMarks(trustChain.getLeafConfiguration())) {
                if (entry.get(TRUST_MARK_TYPE) instanceof final String type && entry.get(TRUST_MARK) instanceof final String trustMark) {
                    try {
                        if (isValidTrustMark(trustMark, type, entityId, trustChainResolver, trustAnchor)) {
                            valid.add(entry);
                        }
                    } catch (final OidcFederationResolutionUnavailableException e) {
                        LOGGER.warn("Trust mark of type [{}] held by [{}] cannot be validated now and is left out: [{}]", type, entityId, e.getMessage());
                        complete = false;
                    }
                }
            }
            return new ValidTrustMarks(List.copyOf(valid), complete);
        } catch (final OidcFederationResolutionUnavailableException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unable to validate the trust marks of [{}]: [{}]", entityId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throwIfCannotTell(mark, entityId, trustChain.getTrustAnchorEntityID());
            return new ValidTrustMarks(List.of(), true);
        }
    }

    /**
     * Fail as unavailable when it could not be told whether a trust mark is valid: since the mark was taken, a request of
     * the running resolution ran out of time or found no room among the running requests, or the trust anchor, whose
     * entity configuration and statements about trust mark issuers the validation needs, could not be reached or
     * answered with a server error. The entity is then not remembered as lacking the trust mark. A trust anchor found
     * unreachable before the validation started, and reached since, tells nothing about it.
     *
     * @param mark          the mark taken when the validation started
     * @param entityId      the entity whose trust marks are validated
     * @param trustAnchorId the trust anchor of the trust chain of the entity
     */
    private static void throwIfCannotTell(final StepMark mark, final EntityID entityId, final EntityID trustAnchorId) {
        if (OidcFederationResolutionLimiter.isUnreachableSince(trustAnchorId, mark.unreachable())
            || OidcFederationResolutionLimiter.isIndeterminateSince(mark.indeterminate())) {
            throw new OidcFederationResolutionUnavailableException("Trust marks of %s cannot be validated now".formatted(entityId));
        }
    }

    /**
     * The entity configuration of the trust anchor of the chain, verified with the keys the trust anchor is trusted with.
     * It is always fetched from the trust anchor, never taken from the chain: a chain a relying party sends may carry a
     * configuration signed with a key the trust anchor has revoked but that is still trusted, listing issuers and owners of
     * its own. Trust marks are thus validated against the trust anchor's own configuration, whatever checks the chain
     * went through before.
     *
     * @param trustChainResolver the trust chain resolver
     * @param trustChain         the trust chain
     * @return the entity configuration of the trust anchor
     * @throws Exception the exception
     */
    protected EntityStatement resolveTrustAnchorConfiguration(final TrustChainResolver trustChainResolver,
                                                              final TrustChain trustChain)
            throws Exception {
        val trustAnchorId = trustChain.getTrustAnchorEntityID();
        val configuration = trustChainResolver.getEntityStatementRetriever().fetchEntityConfiguration(trustAnchorId);
        val keys = trustChainResolver.getTrustAnchors().get(trustAnchorId);
        if (keys == null) {
            configuration.verifySignatureOfSelfStatement();
        } else {
            configuration.verifySignature(keys);
        }
        return configuration;
    }

    /**
     * Whether the trust mark is valid for the entity and type.
     *
     * @param serialized         the trust mark
     * @param type               the trust mark type
     * @param entityId           the entity the trust mark must be about
     * @param trustChainResolver the trust chain resolver
     * @param trustAnchor        the entity configuration of the trust anchor
     * @return true/false
     */
    protected boolean isValidTrustMark(final String serialized, final String type, final EntityID entityId,
                                       final TrustChainResolver trustChainResolver, final EntityStatement trustAnchor) {
        val mark = StepMark.of(trustAnchor.getEntityID());
        try {
            val trustMark = SignedJWT.parse(serialized);
            val issuer = trustMark.getJWTClaimsSet().getIssuer();
            if (!isAcceptedIssuer(trustAnchor, type, issuer)) {
                LOGGER.warn("Trust anchor [{}] does not accept [{}] as an issuer of trust marks of type [{}]",
                    trustAnchor.getEntityID(), issuer, type);
                return false;
            }
            val issuerConfiguration = resolveIssuerConfiguration(trustChainResolver, trustAnchor, issuer);
            val expectedClaims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(entityId.getValue())
                .claim(TRUST_MARK_TYPE, type)
                .build();
            val claims = verify(trustMark, TRUST_MARK_JWT_TYPE, resolveVerificationKeys(trustMark, issuerConfiguration), expectedClaims,
                Set.of(JWTClaimNames.ISSUER, JWTClaimNames.SUBJECT, JWTClaimNames.ISSUED_AT));
            verifyDelegation(trustAnchor, type, issuer, claims);
            verifyStatus(issuerConfiguration, serialized);
            return true;
        } catch (final OidcFederationResolutionUnavailableException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Trust mark of type [{}] held by [{}] is not valid: [{}]", type, entityId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            throwIfCannotTell(mark, entityId, trustAnchor.getEntityID());
            return false;
        }
    }

    /**
     * The keys to verify a trust mark with: the federation keys of its issuer, or, when the trust mark names a key the
     * issuer no longer publishes, that key from the issuer's historical keys (OpenID Federation 1.0, section 8.7),
     * provided the key was never revoked and the trust mark was issued before the key was retired. Trust marks signed
     * before a key rollover thus stay valid for the rest of their life. Historical keys are kept for a minute per issuer,
     * and an issuer whose historical keys are invalid has none for that minute. When they cannot be requested now, because
     * the request fails, is answered with a server error, or runs out of time or room, nothing is kept and the
     * validation is unavailable; when the running
     * resolution has used up its requests, nothing is kept and the trust mark is verified with the federation keys of its
     * issuer.
     *
     * @param trustMark           the trust mark
     * @param issuerConfiguration the entity configuration of the issuer
     * @return the keys
     * @throws Exception the exception
     */
    protected JWKSet resolveVerificationKeys(final SignedJWT trustMark, final EntityStatement issuerConfiguration) throws Exception {
        val issuerKeys = issuerConfiguration.getClaimsSet().getJWKSet();
        val keyId = trustMark.getHeader().getKeyID();
        if (keyId == null || issuerKeys.getKeyByKeyId(keyId) != null) {
            return issuerKeys;
        }
        val issuedAt = trustMark.getJWTClaimsSet().getIssueTime();
        val cacheKey = issuerConfiguration.getEntityID().getValue() + ' '
            + issuerConfiguration.getClaimsSet().getMetadata(EntityType.FEDERATION_ENTITY) + ' ' + issuerKeys.toString(true);
        val cachedKeys = historicalKeys.getIfPresent(cacheKey);
        val keys = cachedKeys == null ? fetchHistoricalKeys(issuerConfiguration, issuerKeys) : cachedKeys;
        if (keys == null) {
            return issuerKeys;
        }
        if (cachedKeys == null) {
            historicalKeys.put(cacheKey, keys);
        }
        return keys
            .stream()
            .filter(key -> keyId.equals(key.getKeyID()))
            .filter(key -> OidcFederationHistoricalKeys.mayHaveSigned(key, issuedAt))
            .findFirst()
            .map(JWKSet::new)
            .orElse(issuerKeys);
    }

    private @Nullable List<JWK> fetchHistoricalKeys(final EntityStatement issuerConfiguration, final JWKSet issuerKeys) {
        try {
            OidcFederationResolutionLimiter.consumeRequest("historical keys of " + issuerConfiguration.getEntityID());
        } catch (final IllegalStateException e) {
            LOGGER.warn("Historical keys of trust mark issuer [{}] are not requested now: [{}]", issuerConfiguration.getEntityID(), e.getMessage());
            return null;
        }
        try {
            return OidcFederationHistoricalKeys.fetch(issuerConfiguration, issuerKeys, httpClient);
        } catch (final OidcFederationHttpClient.RefusedRequestException e) {
            LOGGER.warn("Historical keys of trust mark issuer [{}] may not be requested: [{}]", issuerConfiguration.getEntityID(), e.getMessage());
            return List.of();
        } catch (final IOException e) {
            LOGGER.warn("Historical keys of trust mark issuer [{}] cannot be requested now: [{}]", issuerConfiguration.getEntityID(), e.getMessage());
            throw new OidcFederationResolutionUnavailableException("Historical keys of trust mark issuer %s cannot be requested now"
                .formatted(issuerConfiguration.getEntityID()));
        } catch (final Exception e) {
            LOGGER.warn("Unable to obtain the historical keys of trust mark issuer [{}]: [{}]", issuerConfiguration.getEntityID(), e.getMessage());
            return List.of();
        }
    }

    /**
     * The entity configuration of the trust mark issuer: the trust anchor itself, or the leaf of the issuer's
     * shortest trust chain to the same trust anchor that satisfies the constraints of its subordinate statements.
     *
     * @param trustChainResolver the trust chain resolver
     * @param trustAnchor        the entity configuration of the trust anchor
     * @param issuer             the issuer
     * @return the entity configuration of the issuer
     * @throws Exception the exception
     */
    protected EntityStatement resolveIssuerConfiguration(final TrustChainResolver trustChainResolver,
                                                         final EntityStatement trustAnchor,
                                                         final String issuer)
            throws Exception {
        if (trustAnchor.getEntityID().getValue().equals(issuer)) {
            return trustAnchor;
        }
        return trustChainResolver.resolveTrustChains(new EntityID(issuer))
            .stream()
            .filter(chain -> trustAnchor.getEntityID().equals(chain.getTrustAnchorEntityID()))
            .filter(chain -> OidcFederationTrustChainConstraints.isSatisfiedBy(chain, null))
            .min(Comparator.comparingInt(TrustChain::length))
            .map(TrustChain::getLeafConfiguration)
            .orElseThrow(() -> new BadJWTException("Trust mark issuer " + issuer + " has no trust chain to " + trustAnchor.getEntityID()));
    }

    /**
     * Verify the delegation of a trust mark whose type the trust anchor lists in {@code trust_mark_owners}. A trust mark
     * of such a type must carry a delegation from the owner to its issuer; a delegation whose owner the trust anchor
     * does not name cannot be verified and is refused.
     *
     * @param trustAnchor the entity configuration of the trust anchor
     * @param type        the trust mark type
     * @param issuer      the trust mark issuer
     * @param claims      the claims of the trust mark
     * @throws Exception when the delegation is missing or invalid
     */
    protected void verifyDelegation(final EntityStatement trustAnchor, final String type, final String issuer,
                                    final JWTClaimsSet claims)
            throws Exception {
        val owner = readMapClaim(trustAnchor, TRUST_MARK_OWNERS).get(type);
        val delegation = claims.getStringClaim(DELEGATION);
        if (owner == null && delegation == null) {
            return;
        }
        if (!(owner instanceof final Map<?, ?> ownerEntry)) {
            throw new BadJWTException("Trust mark carries a delegation, but its trust anchor names no owner of type " + type);
        }
        if (delegation == null) {
            throw new BadJWTException("Trust marks of type " + type + " are owned by another entity and need a delegation");
        }
        val ownerKeys = JWKSet.parse(toStringKeyedMap(ownerEntry.get("jwks")));
        val expectedClaims = new JWTClaimsSet.Builder()
            .issuer(String.valueOf(ownerEntry.get(JWTClaimNames.SUBJECT)))
            .subject(issuer)
            .claim(TRUST_MARK_TYPE, type)
            .build();
        verify(SignedJWT.parse(delegation), TRUST_MARK_DELEGATION_JWT_TYPE, ownerKeys, expectedClaims,
            Set.of(JWTClaimNames.ISSUER, JWTClaimNames.SUBJECT, JWTClaimNames.ISSUED_AT));
    }

    /**
     * Ask the trust mark status endpoint of the issuer, when it publishes one, whether the trust mark is active. The
     * endpoint must be an {@code https} URL, as federation endpoints are (section 5.1.1), and the request counts against
     * the budget of the running resolution.
     *
     * @param issuerConfiguration the entity configuration of the issuer
     * @param serialized          the trust mark
     * @throws Exception when the trust mark is not reported active
     */
    protected void verifyStatus(final EntityStatement issuerConfiguration, final String serialized) throws Exception {
        val metadata = readMapClaim(issuerConfiguration, METADATA);
        val endpoint = metadata.get(EntityType.FEDERATION_ENTITY.getValue()) instanceof final Map<?, ?> federationEntity
            ? federationEntity.get(STATUS_ENDPOINT)
            : null;
        if (endpoint == null) {
            return;
        }
        val endpointUri = new URI(endpoint.toString());
        if (!Strings.CI.equals(endpointUri.getScheme(), "https")) {
            throw new BadJWTException("Trust mark status endpoint " + endpoint + " is not an https URL");
        }
        OidcFederationResolutionLimiter.consumeRequest("trust mark status at " + endpoint);
        val request = new HTTPRequest(HTTPRequest.Method.POST, endpointUri);
        request.setEntityContentType(ContentType.APPLICATION_URLENCODED);
        request.setBody(URLUtils.serializeParameters(Map.of(TRUST_MARK, List.of(serialized))));
        val response = sendStatusRequest(request, endpoint);
        if (!response.indicatesSuccess()) {
            throw new BadJWTException("Trust mark status endpoint " + endpoint + " answered " + response.getStatusCode());
        }
        val expectedClaims = new JWTClaimsSet.Builder()
            .issuer(issuerConfiguration.getEntityID().getValue())
            .claim(TRUST_MARK, serialized)
            .claim(STATUS, STATUS_ACTIVE)
            .build();
        verify(SignedJWT.parse(response.getBody()), TRUST_MARK_STATUS_RESPONSE_JWT_TYPE,
            issuerConfiguration.getClaimsSet().getJWKSet(), expectedClaims,
            Set.of(JWTClaimNames.ISSUER, JWTClaimNames.ISSUED_AT, TRUST_MARK, STATUS));
    }

    /**
     * Send the trust mark status request. An endpoint that cannot be reached now, or answers with an error of its own,
     * says nothing about the trust mark: the validation is unavailable rather than failed.
     *
     * @param request  the request
     * @param endpoint the endpoint
     * @return the response
     * @throws IOException when the request may not be sent
     */
    private HTTPResponse sendStatusRequest(final HTTPRequest request, final Object endpoint) throws IOException {
        try {
            val response = httpClient.send(request);
            if (response.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                throw new OidcFederationResolutionUnavailableException("Trust mark status endpoint %s answered %s"
                    .formatted(endpoint, response.getStatusCode()));
            }
            return response;
        } catch (final OidcFederationHttpClient.RefusedRequestException e) {
            throw e;
        } catch (final IOException e) {
            throw new OidcFederationResolutionUnavailableException("Trust mark status endpoint %s cannot be reached now: %s"
                .formatted(endpoint, e.getMessage()));
        }
    }

    private static boolean isAcceptedIssuer(final EntityStatement trustAnchor, final String type,
                                            @Nullable final String issuer) {
        val accepted = readMapClaim(trustAnchor, TRUST_MARK_ISSUERS).get(type);
        return issuer != null && accepted instanceof final List<?> list && (list.isEmpty() || list.contains(issuer));
    }

    private static JWTClaimsSet verify(final SignedJWT jwt, final JOSEObjectType type, final JWKSet keys,
                                       final JWTClaimsSet expectedClaims, final Set<String> requiredClaims)
            throws Exception {
        if (jwt.getHeader().getKeyID() == null) {
            throw new BadJWTException("JWT of type " + type + " does not name its signing key");
        }
        val processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(type));
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(Set.copyOf(JWSAlgorithm.Family.SIGNATURE),
            new ImmutableJWKSet<>(keys)));
        val claimsVerifier = new DefaultJWTClaimsVerifier<SecurityContext>(expectedClaims, requiredClaims);
        claimsVerifier.setMaxClockSkew(MAX_CLOCK_SKEW_SECONDS);
        processor.setJWTClaimsSetVerifier(claimsVerifier);
        val claims = processor.process(jwt, null);
        if (claims.getIssueTime().toInstant().isAfter(Instant.now(Clock.systemUTC()).plusSeconds(MAX_CLOCK_SKEW_SECONDS))) {
            throw new BadJWTException("JWT of type " + type + " is issued in the future");
        }
        return claims;
    }

    private static Map<?, ?> readMapClaim(final EntityStatement statement, final String name) {
        return statement.getClaimsSet().getClaim(name) instanceof final Map<?, ?> value ? value : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> readTrustMarks(final EntityStatement entityConfiguration) {
        if (entityConfiguration.getClaimsSet().getClaim(TRUST_MARKS) instanceof final List<?> entries) {
            return entries.stream().filter(Map.class::isInstance).map(entry -> (Map<String, Object>) entry).toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toStringKeyedMap(final Object value) throws BadJWTException {
        if (value instanceof final Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new BadJWTException("Trust mark owner has no keys");
    }

    /**
     * What the running resolution had found when a step of the validation started: the requests that could not be sent
     * or completed for want of time or room, and the times the trust anchor could not be reached.
     *
     * @param indeterminate the mark of the requests that could not be sent or completed
     * @param unreachable   the mark of the times the trust anchor could not be reached
     */
    private record StepMark(int indeterminate, int unreachable) {
        static StepMark of(final EntityID trustAnchorId) {
            return new StepMark(OidcFederationResolutionLimiter.indeterminateMark(), OidcFederationResolutionLimiter.unreachableMark(trustAnchorId));
        }
    }
}
