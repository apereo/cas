package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;

/**
 * Signs the JWTs the credential issuer publishes about itself, such as status list tokens and signed credential issuer
 * metadata, with the issuer signing key. The key's certificate chain, if any, is sent as the {@code x5c} header without
 * its trust anchor, as HAIP 1.0 requires.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class OidcVerifiableCredentialSigningUtils {

    /**
     * Sign claims with the issuer signing key.
     *
     * @param configurationContext the configuration context
     * @param claims               the claims
     * @param mediaType            the media type, sent as the {@code typ} header
     * @return the signed JWT
     * @throws Throwable the throwable
     */
    public static String sign(final OidcConfigurationContext configurationContext, final JwtClaims claims,
                              final String mediaType)
            throws Throwable {
        val signingKey = Objects.requireNonNull(configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.empty()), "No issuer signing key is available");
        val algorithm = resolveSigningAlgorithm(signingKey);
        return JsonWebTokenSigner.builder()
            .key(Objects.requireNonNull(signingKey.getPrivateKey(), "The issuer signing key has no private key"))
            .keyId(signingKey.getKeyId())
            .algorithm(algorithm)
            .allowedAlgorithms(Set.of(algorithm))
            .mediaType(mediaType)
            .certificateChain(CertUtils.withoutTrustAnchor(signingKey.getCertificateChain()))
            .build()
            .sign(claims);
    }

    /**
     * Signing algorithm for the issuer signing key, by its type and curve.
     *
     * @param signingKey the signing key
     * @return the algorithm
     */
    public static String resolveSigningAlgorithm(final PublicJsonWebKey signingKey) {
        return switch (signingKey) {
            case final EllipticCurveJsonWebKey ecKey when "P-384".equals(ecKey.getCurveName()) -> AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384;
            case final EllipticCurveJsonWebKey ecKey when "P-521".equals(ecKey.getCurveName()) -> AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512;
            case EllipticCurveJsonWebKey _ -> AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;
            case RsaJsonWebKey _ -> AlgorithmIdentifiers.RSA_USING_SHA256;
            default -> throw new IllegalArgumentException("The issuer cannot sign with a " + signingKey.getKeyType() + " key");
        };
    }
}
