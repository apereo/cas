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
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import java.security.cert.X509Certificate;

/**
 * This is {@link BaseOidcVerifiableCredentialEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseOidcVerifiableCredentialEncoder implements OidcVerifiableCredentialEncoder {
    /**
     * Base context of the W3C Verifiable Credentials Data Model 2.0. It defines an {@code @vocab}, so the terms of
     * a credential that uses no further context remain processable as JSON-LD.
     */
    public static final String VCDM_V2_CONTEXT = "https://www.w3.org/ns/credentials/v2";

    protected final OidcConfigurationContext configurationContext;
    /**
     * Types of a W3C verifiable credential issued for a credential configuration: {@code VerifiableCredential} and
     * the configuration's scope, or its id when it has no scope. The issuer metadata publishes the same list as
     * {@code credential_definition.type}, which is how a wallet matches the credential to its configuration.
     *
     * @param configurationId the credential configuration id
     * @param configuration   the credential configuration
     * @return the credential types
     */
    public static List<String> resolveCredentialTypes(final String configurationId,
                                                      final OidcVerifiableCredentialConfigurationProperties configuration) {
        return List.of("VerifiableCredential", StringUtils.defaultIfBlank(configuration.getScope(), configurationId));
    }


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
                claims.put(claimName, toClaimValue(claimValue));
            }
        });
        return claims;
    }

    /**
     * Value of a claim as it is issued. Attribute values usually arrive as text, so text that is a number is issued
     * as a number, but only when the decimal reading of that number gives back the same text. A leading zero would
     * otherwise be read as octal by {@link NumberUtils#createNumber(String)}, turning {@code 0123} into {@code 83}
     * and failing on {@code 08}; such values, like postal codes and identifiers, are issued as text instead.
     *
     * @param value the attribute value
     * @return the claim value
     */
    protected Object toClaimValue(final Object value) {
        if (value instanceof Number) {
            return value;
        }
        val text = value.toString();
        return NumberUtils.isParsable(text) && NumberUtils.createBigDecimal(text).toPlainString().equals(text)
            ? NumberUtils.createNumber(text)
            : value;
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
        final CheckedConsumer<JwtClaims> claimsConsumer)
            throws Throwable {
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
     * permitted set, so an algorithm outside it cannot be used by accident. When the signing key carries
     * a certificate chain, it is sent as the {@code x5c} header (see {@link #resolveCertificateChain(PublicJsonWebKey)}).
     *
     * @param claims            the claims
     * @param configurationId   the credential configuration id
     * @param registeredService the relying party the credential is issued to
     * @return the signed credential
     * @throws Throwable the throwable
     */
    protected String signCredential(final JwtClaims claims, final String configurationId,
                                    final OidcRegisteredService registeredService)
            throws Throwable {
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
            .certificateChain(resolveCertificateChain(signingKey))
            .build()
            .sign(claims);
    }

    /**
     * Certificate chain sent as the credential's {@code x5c} header, taken from the issuer signing key's
     * own {@code x5c}. HAIP 1.0 requires an X.509 chain on issued credentials and forbids the trust anchor in
     * it, so a trailing self-signed certificate is left out unless it is the only one; the SD-JWT VC verifier
     * then takes the issuer key from the leaf. A key without a chain produces no header, and verifiers keep
     * resolving the key by {@code kid} through the issuer's JWKS or JWT VC issuer metadata. A self-signed
     * signing certificate is still sent, with a warning, since HAIP 1.0 requires the signer to be issued by a
     * trust anchor and wallets following it may refuse the credential.
     *
     * @param signingKey the issuer signing key
     * @return the certificate chain, leaf first, possibly empty
     */
    protected List<X509Certificate> resolveCertificateChain(final PublicJsonWebKey signingKey) {
        val certificateChain = CertUtils.withoutTrustAnchor(signingKey.getCertificateChain());
        if (!certificateChain.isEmpty() && CertUtils.isSelfIssued(certificateChain.getFirst())) {
            LOGGER.warn("The credential signing certificate [{}] is self-signed. HAIP 1.0 requires it to be issued by a "
                + "trust anchor, and wallets that follow HAIP may refuse credentials signed with it.",
                certificateChain.getFirst().getSubjectX500Principal().getName());
        }
        return certificateChain;
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
