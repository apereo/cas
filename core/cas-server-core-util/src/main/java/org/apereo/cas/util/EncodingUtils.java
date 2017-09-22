package org.apereo.cas.util;

import com.google.common.base.Throwables;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Map;

/**
 * This is {@link EncodingUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public final class EncodingUtils {

    /**
     * JSON web key parameter that identifies the key..
     */
    public static final String JSON_WEB_KEY = "k";

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtils.class);

    private EncodingUtils() {
    }

    /**
     * Hex decode string.
     *
     * @param data the data
     * @return the string
     */
    public static String hexDecode(final String data) {
        if (StringUtils.isNotBlank(data)) {
            return hexDecode(data.toCharArray());
        }
        return null;
    }

    /**
     * Hex encode string.
     *
     * @param data the data
     * @return the string
     */
    public static String hexEncode(final String data) {
        try {
            final char[] result = Hex.encodeHex(data.getBytes(StandardCharsets.UTF_8));
            return new String(result);
        } catch (final Exception e) {
            return null;
        }
    }
    
    /**
     * Hex encode string.
     *
     * @param data the data
     * @return the string
     */
    public static String hexEncode(final byte[] data) {
        try {
            final char[] result = Hex.encodeHex(data);
            return new String(result);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Hex decode string.
     *
     * @param data the data
     * @return the string
     */
    public static String hexDecode(final char[] data) {
        try {
            final byte[] result = Hex.decodeHex(data);
            return new String(result);
        } catch (final Exception e) {
            return null;
        }
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
     * Url encode a value via UTF-8.
     *
     * @param value the value to encode
     * @return the encoded value
     */
    public static String urlEncode(final String value) {
        return urlEncode(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Url encode a value.
     *
     * @param value    the value to encode
     * @param encoding the encoding
     * @return the encoded value
     */
    public static String urlEncode(final String value, final String encoding) {
        try {
            return URLEncoder.encode(value, encoding);
        } catch (final UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
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
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }


    /**
     * Verify jws signature byte [ ].
     *
     * @param value      the value
     * @param signingKey the signing key
     * @return the byte [ ]
     */
    public static byte[] verifyJwsSignature(final Key signingKey, final byte[] value) {
        try {
            final String asString = new String(value, StandardCharsets.UTF_8);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(asString);
            jws.setKey(signingKey);

            final boolean verified = jws.verifySignature();
            if (verified) {
                final String payload = jws.getPayload();
                LOGGER.trace("Successfully decoded value. Result in Base64-encoding is [{}]", payload);
                return EncodingUtils.decodeBase64(payload);
            }
            return null;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }


    /**
     * Generate octet json web key of given size .
     *
     * @param size the size
     * @return the key
     */
    public static String generateJsonWebKey(final int size) {
        final OctetSequenceJsonWebKey octetKey = OctJwkGenerator.generateJwk(size);
        final Map<String, Object> params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        return params.get(JSON_WEB_KEY).toString();
    }

    /**
     * Sign jws.
     *
     * @param key   the key
     * @param value the value
     * @return the byte [ ]
     */
    public static byte[] signJws(final Key key, final byte[] value) {
        try {
            final String base64 = EncodingUtils.encodeBase64(value);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(base64);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
            jws.setKey(key);
            return jws.getCompactSerialization().getBytes(StandardCharsets.UTF_8);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
