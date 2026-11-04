package org.apereo.cas.oidc.federation.trustmark;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainConstraints;
import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
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
import com.nimbusds.oauth2.sdk.util.URLUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcFederationDefaultTrustMarkValidator}. A trust mark is valid when it is a signed JWT typed
 * {@code trust-mark+jwt} that names its signing key, is about the entity, is of the required type, was issued and
 * has not expired; when the trust anchor of the entity's trust chain accepts its issuer for that type in
 * {@code trust_mark_issuers}; when it verifies with the issuer's federation keys, taken from the trust anchor itself
 * or from the issuer's own trust chain to the same trust anchor; when it carries a valid {@code delegation} for a type
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

    private static final int STATUS_ENDPOINT_TIMEOUT_MILLIS = 5000;

    private final OidcFederationProperties properties;

    @Override
    public boolean hasRequiredTrustMarks(final TrustChainResolver trustChainResolver, final TrustChain trustChain) {
        val requiredTypes = properties.getRequiredTrustMarks();
        if (requiredTypes.isEmpty()) {
            return true;
        }
        val entityId = trustChain.getLeafConfiguration().getEntityID();
        try {
            val trustAnchor = resolveTrustAnchorConfiguration(trustChainResolver, trustChain);
            val trustMarks = readTrustMarks(trustChain.getLeafConfiguration());
            return requiredTypes.stream().allMatch(type -> {
                val valid = trustMarks
                    .stream()
                    .filter(entry -> type.equals(entry.get(TRUST_MARK_TYPE)))
                    .map(entry -> entry.get(TRUST_MARK))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .anyMatch(trustMark -> isValidTrustMark(trustMark, type, entityId, trustChainResolver, trustAnchor));
                if (!valid) {
                    LOGGER.warn("[{}] holds no valid trust mark of type [{}], which is required of relying parties", entityId, type);
                }
                return valid;
            });
        } catch (final Exception e) {
            LOGGER.warn("Unable to validate the trust marks of [{}]: [{}]", entityId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return false;
        }
    }

    /**
     * The entity configuration of the trust anchor of the chain, verified with the keys the trust anchor is trusted with.
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
        val configuration = Objects.requireNonNullElseGet(trustChain.getTrustAnchorConfiguration(),
            Unchecked.supplier(() -> trustChainResolver.getEntityStatementRetriever().fetchEntityConfiguration(trustAnchorId)));
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
            val claims = verify(trustMark, TRUST_MARK_JWT_TYPE, issuerConfiguration.getClaimsSet().getJWKSet(), expectedClaims,
                Set.of(JWTClaimNames.ISSUER, JWTClaimNames.SUBJECT, JWTClaimNames.ISSUED_AT));
            verifyDelegation(trustAnchor, type, issuer, claims);
            verifyStatus(issuerConfiguration, serialized);
            return true;
        } catch (final Exception e) {
            LOGGER.warn("Trust mark of type [{}] held by [{}] is not valid: [{}]", type, entityId, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return false;
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
     * Ask the trust mark status endpoint of the issuer, when it publishes one, whether the trust mark is active.
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
        val request = new HTTPRequest(HTTPRequest.Method.POST, new URI(endpoint.toString()));
        request.setEntityContentType(ContentType.APPLICATION_URLENCODED);
        request.setBody(URLUtils.serializeParameters(Map.of(TRUST_MARK, List.of(serialized))));
        request.setConnectTimeout(STATUS_ENDPOINT_TIMEOUT_MILLIS);
        request.setReadTimeout(STATUS_ENDPOINT_TIMEOUT_MILLIS);
        val response = request.send();
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
}
