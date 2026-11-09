package org.apereo.cas.oidc.federation.trustmark;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationIssuedTrustMarkProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.federation.chain.OidcFederationHistoricalKeys;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * Issues trust marks to the entities that the CAS settings list for each trust mark type, signed with the current
 * federation key and typed {@code trust-mark+jwt} (OpenID Federation 1.0, section 7). Nothing is stored: a trust mark
 * is signed when it is requested, and its status is derived from the settings and the trust mark itself.
 * <p>
 * The status of a trust mark is {@code invalid} when it is not typed as a trust mark or does not verify with the
 * federation keys of this entity, {@code expired} once its {@code exp} has passed, {@code revoked} when its subject no
 * longer holds the type, and {@code active} otherwise. A trust mark that names another issuer, or a type this entity
 * does not issue, is unknown. The federation keys are those this entity publishes and its historical keys (OpenID
 * Federation 1.0, section 8.7): a trust mark signed with a key that has since been retired stays valid for the rest of
 * its life, provided it was issued before the key was retired and the key was never revoked.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationDefaultTrustMarkIssuer implements OidcFederationTrustMarkIssuer {
    /**
     * Status of a trust mark that is valid.
     */
    public static final String STATUS_ACTIVE = "active";

    /**
     * Status of a trust mark that has expired.
     */
    public static final String STATUS_EXPIRED = "expired";

    /**
     * Status of a trust mark whose subject no longer holds its type.
     */
    public static final String STATUS_REVOKED = "revoked";

    /**
     * Status of a trust mark that fails verification.
     */
    public static final String STATUS_INVALID = "invalid";

    private static final String TRUST_MARK_TYPE = "trust_mark_type";

    private static final String TRUST_MARK = "trust_mark";

    private static final String STATUS = "status";

    private static final String LOGO_URI = "logo_uri";

    private static final String REF = "ref";

    private static final String DELEGATION = "delegation";

    private final OidcFederationProperties properties;

    private final OidcIssuerService issuerService;

    private final OidcFederationJsonWebKeystoreService keystoreService;

    @Override
    public boolean isTrustMarkIssuer() {
        return !properties.getIssuedTrustMarks().isEmpty();
    }

    @Override
    public Optional<SignedJWT> issue(final String trustMarkType, final String subject) throws Exception {
        val definition = findDefinition(trustMarkType);
        if (definition.isEmpty() || !definition.get().getSubjects().contains(subject)) {
            return Optional.empty();
        }
        val now = Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
        val claims = new JWTClaimsSet.Builder()
            .issuer(getEntityId())
            .subject(subject)
            .issueTime(Date.from(now))
            .claim(TRUST_MARK_TYPE, trustMarkType);
        val expiration = Beans.newDuration(definition.get().getExpiration());
        if (expiration.isPositive()) {
            claims.expirationTime(Date.from(now.plus(expiration)));
        }
        if (StringUtils.isNotBlank(definition.get().getLogoUri())) {
            claims.claim(LOGO_URI, definition.get().getLogoUri());
        }
        if (StringUtils.isNotBlank(definition.get().getRef())) {
            claims.claim(REF, definition.get().getRef());
        }
        if (StringUtils.isNotBlank(definition.get().getDelegation())) {
            claims.claim(DELEGATION, readDelegation(definition.get().getDelegation()));
        }
        LOGGER.debug("Issuing trust mark of type [{}] to [{}]", trustMarkType, subject);
        return Optional.of(keystoreService.sign(claims.build(), OidcFederationDefaultTrustMarkValidator.TRUST_MARK_JWT_TYPE));
    }

    @Override
    public Optional<List<String>> list(final String trustMarkType, @Nullable final String subject) {
        return findDefinition(trustMarkType)
            .map(definition -> definition.getSubjects()
                .stream()
                .filter(entity -> StringUtils.isBlank(subject) || entity.equals(subject))
                .distinct()
                .toList());
    }

    @Override
    public Optional<SignedJWT> status(final String trustMark) throws Exception {
        val signedTrustMark = SignedJWT.parse(trustMark);
        val claims = signedTrustMark.getJWTClaimsSet();
        val definition = findDefinition(claims.getStringClaim(TRUST_MARK_TYPE));
        if (definition.isEmpty() || !getEntityId().equals(claims.getIssuer())) {
            return Optional.empty();
        }
        val status = determineStatus(signedTrustMark, claims, definition.get());
        LOGGER.debug("Trust mark of type [{}] issued to [{}] is [{}]", definition.get().getTrustMarkType(), claims.getSubject(), status);
        val response = new JWTClaimsSet.Builder()
            .issuer(getEntityId())
            .issueTime(Date.from(Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)))
            .claim(TRUST_MARK, trustMark)
            .claim(STATUS, status)
            .build();
        return Optional.of(keystoreService.sign(response, OidcFederationDefaultTrustMarkValidator.TRUST_MARK_STATUS_RESPONSE_JWT_TYPE));
    }

    /**
     * Determine the status of a trust mark this entity issued.
     *
     * @param trustMark  the trust mark
     * @param claims     the claims of the trust mark
     * @param definition the settings of its type
     * @return the status
     */
    protected String determineStatus(final SignedJWT trustMark, final JWTClaimsSet claims,
                                      final OidcFederationIssuedTrustMarkProperties definition) {
        if (!OidcFederationDefaultTrustMarkValidator.TRUST_MARK_JWT_TYPE.equals(trustMark.getHeader().getType())
            || !isSignedByThisEntity(trustMark, claims)) {
            return STATUS_INVALID;
        }
        val expiration = claims.getExpirationTime();
        if (expiration != null && expiration.toInstant().isBefore(Instant.now(Clock.systemUTC()))) {
            return STATUS_EXPIRED;
        }
        return definition.getSubjects().contains(claims.getSubject()) ? STATUS_ACTIVE : STATUS_REVOKED;
    }

    private boolean isSignedByThisEntity(final SignedJWT trustMark, final JWTClaimsSet claims) {
        try {
            val keyId = trustMark.getHeader().getKeyID();
            if (StringUtils.isBlank(keyId)) {
                return false;
            }
            val key = Optional.ofNullable(keystoreService.toJWKSet().getKeyByKeyId(keyId))
                .or(() -> keystoreService.getHistoricalKeys()
                    .stream()
                    .filter(historicalKey -> keyId.equals(historicalKey.getKeyID()))
                    .filter(historicalKey -> OidcFederationHistoricalKeys.mayHaveSigned(historicalKey, claims.getIssueTime()))
                    .findFirst())
                .orElse(null);
            if (!(key instanceof final AsymmetricJWK asymmetricKey)) {
                return false;
            }
            return trustMark.verify(new DefaultJWSVerifierFactory().createJWSVerifier(trustMark.getHeader(), asymmetricKey.toPublicKey()));
        } catch (final JOSEException e) {
            LOGGER.debug("Trust mark does not verify with the federation keys: [{}]", e.getMessage());
            return false;
        } catch (final Exception e) {
            LOGGER.warn("Unable to read the federation keys: [{}]", e.getMessage());
            return false;
        }
    }

    private Optional<OidcFederationIssuedTrustMarkProperties> findDefinition(@Nullable final String trustMarkType) {
        return properties.getIssuedTrustMarks()
            .stream()
            .filter(definition -> StringUtils.isNotBlank(trustMarkType) && trustMarkType.equals(definition.getTrustMarkType()))
            .findFirst();
    }

    private String getEntityId() {
        return issuerService.determineIssuer(Optional.empty());
    }

    private static String readDelegation(final String configured) throws Exception {
        val value = StringUtils.trim(SpringExpressionLanguageValueResolver.getInstance().resolve(configured));
        return Strings.CS.contains(value, ":")
            ? StringUtils.trim(IOUtils.toString(ResourceUtils.getRawResourceFrom(value).getInputStream(), StandardCharsets.UTF_8))
            : value;
    }
}
