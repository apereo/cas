package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

/**
 * This is {@link BaseOidcVerifiableCredentialEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public abstract class BaseOidcVerifiableCredentialEncoder implements OidcVerifiableCredentialEncoder {
    protected final OidcConfigurationContext configurationContext;

    /**
     * Length of time an issued credential remains valid, per credential configuration.
     *
     * @param configurationId the credential configuration id
     * @return the credential validity
     */
    protected Duration resolveCredentialValidity(final String configurationId) {
        return Beans.newDuration(resolveConfiguration(configurationId).getCredentialValidity());
    }

    protected Map<String, Object> produceClaims(final Principal principal, final OidcVerifiableCredentialValidationContext context) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configurationId = context.resolveConfigurationId();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);
        val claims = new LinkedHashMap<String, Object>();

        configuration.getClaims().forEach((claimName, claimProps) -> {
            val rawValue = principal.getAttributes().get(claimName);

            if (rawValue == null && claimProps.isMandatory()) {
                throw new IllegalArgumentException("Missing required principal attribute for claim %s".formatted(claimName));
            }
            if (rawValue != null) {
                val claimValue = rawValue.size() == 1 ? rawValue.getFirst() : rawValue;
                claims.put(claimName, !(claimValue instanceof Number) && NumberUtils.isParsable(claimValue.toString())
                    ? NumberUtils.createNumber(claimValue.toString())
                    : claimValue);
            }
        });
        return claims;
    }

    protected OidcVerifiableCredentialConfigurationProperties resolveConfiguration(final String configurationId) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val configuration = properties.getIssuer().getCredentialConfigurations().get(configurationId);
        Objects.requireNonNull(configuration, () -> "Unable to locate credential configuration " + configurationId);
        return configuration;
    }

    protected String sign(
        final String sub, final OidcVerifiableCredentialValidationContext context,
        final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof,
        final CheckedConsumer<JwtClaims> claimsConsumer) throws Throwable {
        val oidc = configurationContext.getCasProperties().getAuthn().getOidc();
        val configurationId = context.resolveConfigurationId();

        val issuedAt = NumericDate.now();
        val jwtClaims = new JwtClaims();
        jwtClaims.setSubject(sub);
        jwtClaims.setIssuedAt(issuedAt);
        jwtClaims.setExpirationTime(NumericDate.fromSeconds(
            issuedAt.getValue() + resolveCredentialValidity(configurationId).toSeconds()));

        val nb = Long.valueOf(Beans.newDuration(oidc.getCore().getSkew()).toMinutes()).floatValue();
        jwtClaims.setNotBeforeMinutesInThePast(nb);
        jwtClaims.setJwtId(UUID.randomUUID().toString());

        val issuer = oidc.getCore().getIssuer();
        jwtClaims.setIssuer(issuer);
        jwtClaims.setClaim("vct", issuer + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + configurationId);
        jwtClaims.setClaim("cnf", Map.of("jwk", proof.holderJwk().toJSONObject()));
        
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(),
            context.accessToken().getClientId(),
            OidcRegisteredService.class);
        claimsConsumer.accept(jwtClaims);
        return signCredential(jwtClaims, configurationId, Objects.requireNonNull(registeredService));
    }

    /**
     * Sign the credential with the issuer's own key.
     * <p>
     * A credential is not an ID token and must not be signed as one. It is always signed and never
     * encrypted, whatever the relying party's {@code signIdToken} and {@code encryptIdToken} settings
     * say, and its algorithm comes from the credential configuration's
     * {@code credentialSigningAlgValuesSupported} rather than the client's {@code idTokenSigningAlg},
     * so that what the issuer produces is what the issuer metadata advertises and what a verifier,
     * including this one, will accept. The advertised list is also handed to the signer as its
     * permitted set, so an algorithm outside it cannot be used by accident.
     *
     * @param claims            the claims
     * @param configurationId   the credential configuration id
     * @param registeredService the relying party the credential is issued to
     * @return the signed credential
     * @throws Throwable the throwable
     */
    protected String signCredential(final JwtClaims claims, final String configurationId,
                                    final OidcRegisteredService registeredService) throws Throwable {
        val configuration = resolveConfiguration(configurationId);
        val signingKey = configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(registeredService));
        Objects.requireNonNull(signingKey, "No signing key is available to sign the verifiable credential");
        Objects.requireNonNull(signingKey.getPrivateKey(), "The credential signing key has no private key");

        return JsonWebTokenSigner.builder()
            .key(signingKey.getPrivateKey())
            .keyId(signingKey.getKeyId())
            .algorithm(resolveSigningAlgorithm(configuration, signingKey, registeredService))
            .allowedAlgorithms(new LinkedHashSet<>(resolveSupportedSigningAlgorithms(configuration, registeredService)))
            .mediaType(getFormat().getValue())
            .headers(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()))
            .build()
            .sign(claims);
    }

    /**
     * Algorithm used to sign this credential: the first algorithm available to the service that the
     * issuer's signing key can actually perform. Order in {@code credentialSigningAlgValuesSupported} is
     * therefore a preference the deployment expresses. Override to select differently.
     *
     * @param configuration     the credential configuration
     * @param signingKey        the issuer signing key
     * @param registeredService the registered service
     * @return the signing algorithm
     */
    protected String resolveSigningAlgorithm(final OidcVerifiableCredentialConfigurationProperties configuration,
                                             final PublicJsonWebKey signingKey,
                                             final OidcRegisteredService registeredService) {
        val supported = resolveSupportedSigningAlgorithms(configuration, registeredService);
        return supported
            .stream()
            .filter(algorithm -> isAlgorithmSupportedByKey(algorithm, signingKey))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "None of the credential signing algorithms %s can be used with a %s signing key"
                    .formatted(supported, signingKey.getKeyType())));
    }

    /**
     * Algorithms this service may have its credentials signed with: the configuration's advertised list,
     * narrowed by the service's verifiable credentials policy when that policy names any. The
     * configuration's order is preserved, since it is the deployment's preference, and a policy that
     * names none leaves the service with everything the configuration advertises.
     * <p>
     * The returned list is a copy. The configuration's own list belongs to the shared configuration
     * bean, so narrowing it in place would leak one service's policy into every other service.
     *
     * @param configuration     the credential configuration
     * @param registeredService the registered service
     * @return the supported signing algorithms
     */
    protected List<String> resolveSupportedSigningAlgorithms(
        final OidcVerifiableCredentialConfigurationProperties configuration,
        final OidcRegisteredService registeredService) {
        val supported = new ArrayList<>(configuration.getCredentialSigningAlgValuesSupported());
        val policy = registeredService.getVerifiableCredentialsPolicy();
        if (policy != null && policy.getCredentialSigningAlgValuesSupported() != null
            && !policy.getCredentialSigningAlgValuesSupported().isEmpty()) {
            supported.retainAll(policy.getCredentialSigningAlgValuesSupported());
        }
        return supported;
    }

    /**
     * Can the issuer's signing key perform this algorithm? A configuration may advertise algorithms for
     * several key types; only those matching the key CAS actually signs with are candidates.
     *
     * @param algorithm  the algorithm
     * @param signingKey the issuer signing key
     * @return true/false
     */
    protected boolean isAlgorithmSupportedByKey(final String algorithm, final PublicJsonWebKey signingKey) {
        if (signingKey instanceof RsaJsonWebKey) {
            return algorithm.startsWith("RS") || algorithm.startsWith("PS");
        }
        if (signingKey instanceof EllipticCurveJsonWebKey) {
            return algorithm.startsWith("ES");
        }
        return "EdDSA".equals(algorithm);
    }
}
