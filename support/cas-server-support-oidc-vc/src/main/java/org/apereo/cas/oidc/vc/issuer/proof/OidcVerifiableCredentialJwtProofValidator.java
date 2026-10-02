package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialJwtProofValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialJwtProofValidator implements OidcVerifiableCredentialProofValidator {
    /**
     * Media type every OID4VCI JWT proof must declare, so that a token minted
     * for a different protocol can never be replayed as a proof of possession.
     */
    private static final String PROOF_JWT_TYPE = "openid4vci-proof+jwt";

    private static final String BINDING_METHOD_JWK = "jwk";

    private static final String DID_PREFIX = "did:";

    private static final String DID_JWK_PREFIX = "did:jwk:";

    private static final int SECONDS_IN_FUTURE = 30;
    private static final int MINUTES_IN_PAST = 5;

    private final CasConfigurationProperties casProperties;
    private final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

    /**
     * Every way this can fail is either {@code invalid_nonce} or {@code invalid_proof} as far as
     * OpenID4VCI is concerned, so a malformed JWT, a JOSE failure or anything else unexpected is
     * turned into {@code invalid_proof} here rather than escaping as a generic exception the
     * credential endpoint would have to report as a plain bad request.
     * <p>
     * When a credential configuration is named, the proof must also use one of the proof signing algorithms
     * and one of the cryptographic binding methods that configuration advertises, so that what the issuer
     * metadata promises a wallet is exactly what is accepted.
     */
    @Override
    public VerifiableCredentialProofResult validate(final String proofJwt,
                                                    final @Nullable String configurationId,
                                                    final Set<String> consumedNonces) throws Exception {
        try {
            val signedJwt = SignedJWT.parse(proofJwt);
            val configuration = resolveConfiguration(configurationId);

            verifyType(signedJwt);
            val holderJwk = resolveHolderKey(signedJwt, configuration);
            verifyAlgorithm(signedJwt, holderJwk, configuration);
            verifySignature(signedJwt, holderJwk);
            verifyAudience(signedJwt);
            verifyFreshness(signedJwt);
            val nonce = verifyNonce(signedJwt, consumedNonces);

            val claims = signedJwt.getJWTClaimsSet();
            return new VerifiableCredentialProofResult(
                "jwt",
                claims.getJWTID(),
                claims.getSubject(),
                holderJwk,
                nonce
            );
        } catch (final OidcVerifiableCredentialProofException e) {
            throw e;
        } catch (final Exception e) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                StringUtils.defaultIfBlank(e.getMessage(), "Proof of possession could not be validated"));
        }
    }

    protected void verifyType(final SignedJWT signedJwt) {
        val type = signedJwt.getHeader().getType();
        if (type == null || !PROOF_JWT_TYPE.equals(type.toString())) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT type must be " + PROOF_JWT_TYPE);
        }
    }

    protected @Nullable String verifyNonce(final SignedJWT signedJwt, final Set<String> consumedNonces) throws Exception {
        val claims = signedJwt.getJWTClaimsSet();
        val nonce = claims.getStringClaim("nonce");
        if (nonce == null) {
            throw OidcVerifiableCredentialProofException.invalidNonce("Proof nonce is missing");
        }
        if (consumedNonces.contains(nonce)) {
            return nonce;
        }
        if (!oidcVerifiableCredentialNonceService.consume(nonce)) {
            throw OidcVerifiableCredentialProofException.invalidNonce("Proof nonce %s is invalid, expired or already used".formatted(nonce));
        }
        consumedNonces.add(nonce);
        return nonce;
    }

    /**
     * Credential configuration the proof is presented for.
     *
     * @param configurationId the credential configuration id, or null when the caller names none
     * @return the credential configuration, or null when none is named
     */
    protected @Nullable OidcVerifiableCredentialConfigurationProperties resolveConfiguration(final @Nullable String configurationId) {
        if (StringUtils.isBlank(configurationId)) {
            return null;
        }
        val configuration = casProperties.getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations().get(configurationId);
        if (configuration == null) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Credential configuration %s is not published by this issuer".formatted(configurationId));
        }
        return configuration;
    }

    /**
     * Resolve the key the credential is to be bound to, per OpenID4VCI 1.0 appendix F.1: the {@code jwk} header,
     * the leaf certificate of the {@code x5c} header, or a {@code kid} that is a {@code did:jwk} DID URL.
     * A {@code kid} next to a {@code jwk} is ignored rather than refused, because wallets commonly send the key's
     * own identifier along with it; {@code jwk} and {@code x5c} together are ambiguous and refused. The key must
     * arrive by a binding method the configuration advertises: {@code jwk} and {@code x5c} under {@code jwk},
     * a DID under its own method, such as {@code did:jwk}.
     *
     * @param signedJwt     the proof JWT
     * @param configuration the credential configuration, or null when the caller names none
     * @return the holder key
     * @throws Exception the exception
     */
    protected JWK resolveHolderKey(final SignedJWT signedJwt,
                                   final @Nullable OidcVerifiableCredentialConfigurationProperties configuration) throws Exception {
        val header = signedJwt.getHeader();
        val embeddedKey = header.getJWK();
        val certificateChain = header.getX509CertChain();
        val hasCertificateChain = certificateChain != null && !certificateChain.isEmpty();
        if (embeddedKey != null && hasCertificateChain) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT must not carry both the jwk and x5c headers");
        }
        if (embeddedKey != null || hasCertificateChain) {
            verifyBindingMethod(configuration, BINDING_METHOD_JWK);
            val holderKey = embeddedKey != null
                ? embeddedKey
                : resolveCertificateKey(Objects.requireNonNull(certificateChain).getFirst().decode());
            return verifyPublicKey(holderKey);
        }
        val keyId = header.getKeyID();
        if (keyId != null && keyId.startsWith(DID_PREFIX)) {
            verifyBindingMethod(configuration, DID_PREFIX + StringUtils.substringBefore(keyId.substring(DID_PREFIX.length()), ":"));
            return verifyPublicKey(resolveDecentralizedIdentifierKey(keyId));
        }
        throw OidcVerifiableCredentialProofException.invalidProof(
            "Proof JWT must identify the holder key with a jwk, x5c or DID kid header");
    }

    /**
     * Public key of the leaf certificate presented in the {@code x5c} header. Only the key members are kept,
     * so the credential's {@code cnf} carries the key and not the certificate chain. The certificate must be
     * within its validity period; trusting its issuer is left to key attestation, which this validator does not
     * evaluate.
     *
     * @param encodedCertificate the DER encoded leaf certificate
     * @return the holder key
     * @throws Exception the exception
     */
    protected JWK resolveCertificateKey(final byte[] encodedCertificate) throws Exception {
        val certificate = X509CertUtils.parse(encodedCertificate);
        if (certificate == null) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT x5c header does not hold a valid certificate");
        }
        certificate.checkValidity();
        return JWK.parse(new LinkedHashMap<>(JWK.parse(certificate).getRequiredParams()));
    }

    /**
     * Key named by a DID URL in the {@code kid} header. Only {@code did:jwk} can be resolved, since it carries
     * the key itself; any other DID method is refused rather than trusted unresolved.
     *
     * @param keyId the DID URL
     * @return the holder key
     * @throws Exception the exception
     */
    protected JWK resolveDecentralizedIdentifierKey(final String keyId) throws Exception {
        if (!keyId.startsWith(DID_JWK_PREFIX)) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Proof key identifier %s uses a DID method that cannot be resolved; only did:jwk is supported".formatted(keyId));
        }
        val encodedKey = StringUtils.substringBefore(keyId.substring(DID_JWK_PREFIX.length()), "#");
        return JWK.parse(new Base64URL(encodedKey).decodeToString());
    }

    protected void verifyBindingMethod(final @Nullable OidcVerifiableCredentialConfigurationProperties configuration,
                                       final String bindingMethod) {
        if (configuration != null && !configuration.getCryptographicBindingMethodsSupported().contains(bindingMethod)) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Cryptographic binding method %s is not supported by this credential configuration".formatted(bindingMethod));
        }
    }

    protected JWK verifyPublicKey(final JWK holderKey) {
        if (holderKey.isPrivate()) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT holder key must not contain private key material");
        }
        return holderKey;
    }

    protected void verifySignature(final SignedJWT signedJwt, final JWK holderJwk) throws Exception {
        val verified = switch (holderJwk) {
            case final RSAKey rsaKey -> signedJwt.verify(new RSASSAVerifier(rsaKey));
            case final ECKey ecKey -> signedJwt.verify(new ECDSAVerifier(ecKey));
            case final OctetKeyPair octetKeyPair -> verifyEdwardsCurveSignature(signedJwt, octetKeyPair);
            default -> false;
        };
        if (!verified) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT signature validation failed");
        }
    }

    /**
     * Verify an EdDSA proof with the JDK's own Edwards-curve support through jose4j. Nimbus verifies EdDSA
     * through Google Tink, which CAS does not depend on. The algorithm is already known to be
     * {@code EdDSA} here, see {@link #verifyAlgorithm}.
     *
     * @param signedJwt the proof JWT
     * @param holderJwk the holder key
     * @return true if the signature verifies
     */
    protected boolean verifyEdwardsCurveSignature(final SignedJWT signedJwt, final OctetKeyPair holderJwk) {
        val holderKey = EncodingUtils.newJsonWebKey(holderJwk.toPublicJWK().toJSONString()).getKey();
        return EncodingUtils.verifyJwsSignature(holderKey, signedJwt.serialize()) != null;
    }

    protected void verifyAudience(final SignedJWT signedJwt) throws ParseException {
        val audiences = signedJwt.getJWTClaimsSet().getAudience();
        val credentialIssuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
        if (audiences == null || !audiences.contains(credentialIssuer)) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof audience does not match credential issuer");
        }
    }

    protected void verifyAlgorithm(final SignedJWT signedJwt, final JWK holderJwk,
                                   final @Nullable OidcVerifiableCredentialConfigurationProperties configuration) {
        val alg = signedJwt.getHeader().getAlgorithm();
        if (alg == null || Algorithm.NONE.equals(alg)) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT algorithm is invalid");
        }
        if (configuration != null && !configuration.getProofSigningAlgValuesSupported().contains(alg.getName())) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Proof JWT algorithm %s is not supported by this credential configuration".formatted(alg.getName()));
        }
        val matchesKey = switch (holderJwk) {
            case RSAKey _ -> JWSAlgorithm.Family.RSA.contains(alg);
            case ECKey _ -> JWSAlgorithm.Family.EC.contains(alg);
            case OctetKeyPair _ -> JWSAlgorithm.EdDSA.equals(alg);
            default -> false;
        };
        if (!matchesKey) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Proof JWT algorithm %s does not match the %s holder key".formatted(alg.getName(), holderJwk.getKeyType()));
        }
    }

    protected void verifyFreshness(final SignedJWT signedJwt) throws ParseException {
        val claims = signedJwt.getJWTClaimsSet();
        val issuedAt = claims.getIssueTime();
        if (issuedAt == null) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT is missing iat");
        }
        val now = Instant.now(Clock.systemUTC());
        val iat = issuedAt.toInstant();
        if (iat.isAfter(now.plusSeconds(SECONDS_IN_FUTURE))) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof iat is in the future");
        }
        if (iat.isBefore(now.minus(Duration.ofMinutes(MINUTES_IN_PAST)))) {
            throw OidcVerifiableCredentialProofException.invalidProof("Proof JWT is too old");
        }
    }
}
