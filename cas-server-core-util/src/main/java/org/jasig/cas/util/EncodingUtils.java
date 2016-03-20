package org.jasig.cas.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Formatter;
import java.util.stream.IntStream;

/**
 * This is {@link EncodingUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 4.3
 */
public final class EncodingUtils {

    private EncodingUtils() {
    }

    /**
     * Hex encode the given byte[] as a string.
     *
     * @param data the byte array to encode
     * @return the encoded string
     */
    public static String hexEncode(final byte[] data) {
        final StringBuilder sb = new StringBuilder();
        final Formatter f = new Formatter(sb);
        IntStream.range(0, data.length).forEach(i -> f.format("%02x", data[i]));
        return sb.toString();
    }

    /**
     * Base64-encode the given byte[] as a string.
     *
     * @param data the byte array to encode
     * @return the encoded string
     */
    public static String encodeBase64(final byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the encoded array
     */
    public static byte[] decodeBase64(final String data) {
        return Base64.getDecoder().decode(data);
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the encoded array
     */
    public static byte[] decodeBase64(final byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    /**
     * Base64-encode the given byte[] as a byte[].
     *
     * @param data the byte array to encode
     * @return the byte[] in base64
     */
    public static byte[] encodeBase64ToByteArray(final byte[] data) {
        return Base64.getEncoder().encode(data);
    }


    /**
     * Url encode a value.
     *
     * @param value the value to encode
     * @return the encoded value
     */
    public static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Url decode a value.
     *
     * @param value the value to decode
     * @return the decoded value
     */
    public static String urlDecode(final String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
