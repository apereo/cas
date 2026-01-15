package org.apereo.cas.support.oauth.web.response.accesstoken;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.jose4j.jws.AlgorithmIdentifiers;

/**
 * This is {@link OAuth20TokenHashGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SuperBuilder
@Getter
@Slf4j
public class OAuth20TokenHashGenerator {
    private final String token;

    private final String algorithm;

    private final RegisteredService registeredService;

    /**
     * Generate string.
     *
     * @return the string
     */
    public String generate() {
        val tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (AlgorithmIdentifiers.NONE.equalsIgnoreCase(this.algorithm)) {
            LOGGER.debug("Signing algorithm specified by service [{}] is unspecified/none", registeredService.getServiceId());
            return EncodingUtils.encodeUrlSafeBase64(tokenBytes);
        }
        val alg = determineSigningHashAlgorithm();
        LOGGER.debug("Digesting access token hash via algorithm [{}]", alg);
        val digested = DigestUtils.rawDigest(alg, tokenBytes);
        val hashBytesLeftHalf = Arrays.copyOf(digested, digested.length / 2);
        return EncodingUtils.encodeUrlSafeBase64(hashBytesLeftHalf);
    }

    /**
     * Gets signing hash algorithm.
     *
     * @return the signing hash algorithm
     */
    protected String determineSigningHashAlgorithm() {
        LOGGER.debug("Signing algorithm specified is [{}]", this.algorithm);
        if (AlgorithmIdentifiers.HMAC_SHA512.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_USING_SHA512.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_PSS_USING_SHA512.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512.equalsIgnoreCase(algorithm)) {
            return MessageDigestAlgorithms.SHA_512;
        }
        if (AlgorithmIdentifiers.HMAC_SHA384.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_USING_SHA384.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_PSS_USING_SHA384.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384.equalsIgnoreCase(algorithm)) {
            return MessageDigestAlgorithms.SHA_384;
        }
        if (AlgorithmIdentifiers.HMAC_SHA256.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_USING_SHA256.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.RSA_PSS_USING_SHA256.equalsIgnoreCase(algorithm)
            || AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256.equalsIgnoreCase(algorithm)) {
            return MessageDigestAlgorithms.SHA_256;
        }
        throw new IllegalArgumentException("Could not determine the hash algorithm for token");
    }
}
