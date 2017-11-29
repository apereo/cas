package org.apereo.cas.util;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This is {@link DigestUtils}
 * that encapsulates common compression calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public final class DigestUtils {
    private static final int ABBREVIATE_MAX_WIDTH = 125;
    
    private DigestUtils() {
    }

    /**
     * Computes hex encoded SHA512 digest.
     *
     * @param data data to be hashed
     * @return sha-512 hash
     */
    public static String sha512(final String data) {
        return digest(MessageDigestAlgorithms.SHA_512, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes hex encoded SHA256 digest.
     *
     * @param data data to be hashed
     * @return sha-256 hash
     */
    public static String sha256(final String data) {
        return digest(MessageDigestAlgorithms.SHA_256, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes hex encoded SHA digest.
     *
     * @param data data to be hashed
     * @return sha hash
     */
    public static String sha(final String data) {
        return digest(MessageDigestAlgorithms.SHA_1, data);
    }

    /**
     * Computes SHA digest.
     *
     * @param data data to be hashed
     * @return sha hash
     */
    public static byte[] sha(final byte[] data) {
        return rawDigest(MessageDigestAlgorithms.SHA_1, data);
    }

    /**
     * Sha base 64 string.
     *
     * @param salt the salt
     * @param data the data
     * @return the string
     */
    public static String shaBase64(final String salt, final String data) {
        return shaBase64(salt, data, null);
    }

    /**
     * Sha base 64 string.
     *
     * @param salt the salt
     * @param data the data
     * @param separator a string separator, if any
     * @return the string
     */
    public static String shaBase64(final String salt, final String data, final String separator) {
        final byte[] result = rawDigest(MessageDigestAlgorithms.SHA_1, salt, separator == null ? data : data + separator);
        return EncodingUtils.encodeBase64(result);
    }
    
    /**
     * Computes hex encoded digest.
     *
     * @param alg  Digest algorithm to use
     * @param data data to be hashed
     * @return hex encoded hash
     */
    public static String digest(final String alg, final String data) {
        return digest(alg, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes hex encoded digest.
     *
     * @param alg  Digest algorithm to use
     * @param data data to be hashed
     * @return hex encoded hash
     */
    public static String digest(final String alg, final byte[] data) {
        return EncodingUtils.hexEncode(rawDigest(alg, data));
    }

    /**
     * Computes digest.
     *
     * @param alg  Digest algorithm to use
     * @param data data to be hashed
     * @return hash
     */
    public static byte[] rawDigest(final String alg, final byte[] data) {
        try {
            final MessageDigest digest = getMessageDigestInstance(alg);
            return digest.digest(data);
        } catch (final Exception cause) {
            throw new SecurityException(cause);
        }
    }

    
    /**
     * Raw digest byte [ ].
     *
     * @param alg  the alg
     * @param salt the salt
     * @param data the data
     * @return the byte [ ]
     */
    public static byte[] rawDigest(final String alg, final String salt, final String... data) {
        try {
            final MessageDigest digest = getMessageDigestInstance(alg);
            Arrays.stream(data).forEach(d -> digest.update(d.getBytes()));
            return digest.digest(salt.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception cause) {
            throw new SecurityException(cause);
        }
    }

    /**
     * Abbreviate string.
     *
     * @param str the str
     * @return the string
     */
    public static String abbreviate(final String str) {
        return StringUtils.abbreviate(str, ABBREVIATE_MAX_WIDTH);
    }
    
    private static MessageDigest getMessageDigestInstance(final String alg) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance(alg);
        digest.reset();
        return digest;
    }
}
