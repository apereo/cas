package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link DigestUtils}
 * that encapsulates common compression calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@UtilityClass
public class DigestUtils {
    private static final int ABBREVIATE_MAX_WIDTH = 125;

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
     * @param salt      the salt
     * @param data      the data
     * @param separator a string separator, if any
     * @param chunked   the chunked
     * @return the string
     */
    public static String shaBase64(final String salt, final String data, final String separator, final boolean chunked) {
        val result = rawDigest(MessageDigestAlgorithms.SHA_1, salt, Optional.ofNullable(separator).map(s -> data + s).orElse(data));
        return EncodingUtils.encodeBase64(result, chunked);
    }

    /**
     * Sha base 64 string.
     *
     * @param salt      the salt
     * @param data      the data
     * @param separator the separator
     * @return the string
     */
    public static String shaBase64(final String salt, final String data, final String separator) {
        val result = rawDigest(MessageDigestAlgorithms.SHA_1, salt, Optional.ofNullable(separator).map(s -> data + s).orElse(data));
        return EncodingUtils.encodeBase64(result);
    }

    /**
     * Sha base 32 string.
     *
     * @param salt      the salt
     * @param data      the data
     * @param separator a string separator, if any
     * @param chunked   the chunked
     * @return the string
     */
    public static String shaBase32(final String salt, final String data, final String separator, final boolean chunked) {
        val result = rawDigest(MessageDigestAlgorithms.SHA_1, salt, Optional.ofNullable(separator).map(s -> data + s).orElse(data));
        return EncodingUtils.encodeBase32(result, chunked);
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
     * @param data data to be hashed
     * @return hash byte []
     */
    public static byte[] rawDigestSha256(final String data) {
        try {
            return rawDigest(MessageDigestAlgorithms.SHA_256, data.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception cause) {
            throw new SecurityException(cause);
        }
    }

    /**
     * Raw digest.
     *
     * @param alg  the alg
     * @param data the data
     * @return the byte [ ]
     */
    public static byte[] rawDigest(final String alg, final byte[] data) {
        try {
            val digest = getMessageDigestInstance(alg);
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
            val digest = getMessageDigestInstance(alg);
            Arrays.stream(data).forEach(d -> digest.update(d.getBytes(StandardCharsets.UTF_8)));
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
        val digest = MessageDigest.getInstance(alg);
        digest.reset();
        return digest;
    }
}
