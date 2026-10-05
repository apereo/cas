package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import java.security.cert.X509Certificate;

/**
 * Validates key attestations, per OpenID4VCI 1.0 Appendix D, with the X.509 key resolution HAIP 1.0 (section 4.5.1)
 * requires: a {@code key-attestation+jwt} signed by the key of the first certificate in its {@code x5c} header, a chain
 * that leads to one of the configured trust anchors without including it, and a signer that is not self-signed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcVerifiableCredentialKeyAttestationValidator {
    private static final Set<String> KEY_ATTESTATION_TYPES = Set.of("key-attestation+jwt", "application/key-attestation+jwt");

    private static final int SECONDS_IN_FUTURE = 30;

    private final Set<TrustAnchor> trustAnchors;

    public OidcVerifiableCredentialKeyAttestationValidator(final CasConfigurationProperties casProperties) {
        this.trustAnchors = loadTrustAnchors(casProperties.getAuthn().getOidc().getVc().getIssuer().getKeyAttestation().getTrustAnchors());
    }

    /**
     * Whether key attestations can be verified at all, which needs at least one trust anchor.
     *
     * @return true/false
     */
    public boolean isEnabled() {
        return !trustAnchors.isEmpty();
    }

    /**
     * Validate a key attestation and check it against what the credential configuration requires: when the
     * configuration lists accepted {@code key_storage} or {@code user_authentication} levels, the attestation must name
     * at least one of each. A {@code status} claim is accepted with a warning, since revocation through a Token Status
     * List is not checked yet. Every failure is an {@code invalid_proof}.
     *
     * @param keyAttestation the key attestation JWT
     * @param configuration  the credential configuration, or null when the caller names none
     * @return the validated key attestation
     */
    public KeyAttestation validate(final String keyAttestation,
                                   final @Nullable OidcVerifiableCredentialConfigurationProperties configuration) {
        if (!isEnabled()) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestations cannot be verified: no trust anchors are configured");
        }
        try {
            val signedJwt = SignedJWT.parse(keyAttestation);
            val type = signedJwt.getHeader().getType();
            if (type == null || !KEY_ATTESTATION_TYPES.contains(type.toString())) {
                throw OidcVerifiableCredentialProofException.invalidProof("Key attestation type must be key-attestation+jwt");
            }
            val signer = verifyCertificateChain(signedJwt);
            verifySignature(signedJwt, signer);

            val claims = signedJwt.getJWTClaimsSet();
            val now = Instant.now(Clock.systemUTC());
            if (claims.getIssueTime() == null || claims.getIssueTime().toInstant().isAfter(now.plusSeconds(SECONDS_IN_FUTURE))) {
                throw OidcVerifiableCredentialProofException.invalidProof("Key attestation iat is missing or in the future");
            }
            if (claims.getExpirationTime() != null && !claims.getExpirationTime().toInstant().isAfter(now)) {
                throw OidcVerifiableCredentialProofException.invalidProof("Key attestation has expired");
            }
            val attestedKeys = readAttestedKeys(claims.getClaim("attested_keys"));
            val keyStorage = readStringList(claims, "key_storage");
            val userAuthentication = readStringList(claims, "user_authentication");
            if (claims.getClaim("status") != null) {
                LOGGER.warn("Key attestation from [{}] carries a status claim; its revocation status is not checked",
                    signer.getSubjectX500Principal().getName());
            }
            if (configuration != null) {
                verifyRequirement("key_storage", configuration.getKeyAttestations().getKeyStorage(), keyStorage);
                verifyRequirement("user_authentication", configuration.getKeyAttestations().getUserAuthentication(), userAuthentication);
            }
            return new KeyAttestation(signedJwt.getHeader().getAlgorithm().getName(), attestedKeys,
                claims.getStringClaim("nonce"), keyStorage, userAuthentication);
        } catch (final OidcVerifiableCredentialProofException e) {
            throw e;
        } catch (final Exception e) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestation could not be validated: " + e.getMessage());
        }
    }

    /**
     * The {@code x5c} chain of the key attestation must validate, with PKIX, to a configured trust anchor. The anchor
     * itself must not be part of the chain and the signer must not be self-signed (HAIP 1.0 section 4.5.1), so no
     * certificate in the chain may be self-issued.
     *
     * @param signedJwt the key attestation
     * @return the signing certificate
     * @throws Exception the exception
     */
    protected X509Certificate verifyCertificateChain(final SignedJWT signedJwt) throws Exception {
        val encodedChain = signedJwt.getHeader().getX509CertChain();
        if (encodedChain == null || encodedChain.isEmpty()) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestation carries no x5c certificate chain");
        }
        val chain = new ArrayList<X509Certificate>();
        for (val encoded : encodedChain) {
            val certificate = X509CertUtils.parse(encoded.decode());
            if (certificate == null || CertUtils.isSelfIssued(certificate)) {
                throw OidcVerifiableCredentialProofException.invalidProof(
                    "Key attestation x5c must hold valid certificates, without the trust anchor or a self-signed signer");
            }
            chain.add(certificate);
        }
        val parameters = new PKIXParameters(trustAnchors);
        parameters.setRevocationEnabled(false);
        CertPathValidator.getInstance("PKIX").validate(CertUtils.getCertificateFactory().generateCertPath(chain), parameters);
        return chain.getFirst();
    }

    protected void verifySignature(final SignedJWT signedJwt, final X509Certificate signer) throws Exception {
        val algorithm = signedJwt.getHeader().getAlgorithm();
        if (algorithm == null || Algorithm.NONE.equals(algorithm)) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestation algorithm is invalid");
        }
        val verified = switch (JWK.parse(signer)) {
            case final ECKey ecKey when JWSAlgorithm.Family.EC.contains(algorithm) -> signedJwt.verify(new ECDSAVerifier(ecKey));
            case final RSAKey rsaKey when JWSAlgorithm.Family.RSA.contains(algorithm) -> signedJwt.verify(new RSASSAVerifier(rsaKey));
            default -> false;
        };
        if (!verified) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestation signature validation failed");
        }
    }

    protected static List<JWK> readAttestedKeys(final @Nullable Object attestedKeys) throws Exception {
        if (!(attestedKeys instanceof final List<?> values) || values.isEmpty()) {
            throw OidcVerifiableCredentialProofException.invalidProof("Key attestation attests no keys");
        }
        val keys = new ArrayList<JWK>();
        for (val value : values) {
            if (!(value instanceof final Map<?, ?> members)) {
                throw OidcVerifiableCredentialProofException.invalidProof("Key attestation attested key is invalid");
            }
            val key = JWK.parse((Map<String, Object>) members);
            if (key.isPrivate()) {
                throw OidcVerifiableCredentialProofException.invalidProof("Key attestation attested key must not contain private key material");
            }
            keys.add(key);
        }
        return keys;
    }

    protected static List<String> readStringList(final JWTClaimsSet claims, final String claimName) throws Exception {
        val values = claims.getStringListClaim(claimName);
        return values != null ? values : List.of();
    }

    protected static void verifyRequirement(final String claimName, final List<String> accepted, final List<String> attested) {
        if (!accepted.isEmpty() && attested.stream().noneMatch(accepted::contains)) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "Key attestation %s %s does not meet the required %s".formatted(claimName, attested, accepted));
        }
    }

    private static Set<TrustAnchor> loadTrustAnchors(final List<String> locations) {
        return locations
            .stream()
            .map(location -> FunctionUtils.doUnchecked(() -> readCertificates(location)))
            .flatMap(List::stream)
            .map(certificate -> new TrustAnchor(certificate, null))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static List<X509Certificate> readCertificates(final String location) throws Exception {
        try (val input = ResourceUtils.getResourceFrom(location).getInputStream()) {
            return CertUtils.getCertificateFactory().generateCertificates(input)
                .stream()
                .map(X509Certificate.class::cast)
                .toList();
        }
    }

    /**
     * A validated key attestation.
     *
     * @param algorithm          the signing algorithm of the attestation
     * @param attestedKeys       the attested keys
     * @param nonce              the nonce, if any
     * @param keyStorage         the attested key storage levels
     * @param userAuthentication the attested user authentication levels
     */
    public record KeyAttestation(String algorithm, List<JWK> attestedKeys, @Nullable String nonce,
                                 List<String> keyStorage, List<String> userAuthentication) {
    }
}
