package org.apereo.cas.util;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.crypto.DecryptionException;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import javax.crypto.Cipher;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * This is {@link EncodingUtils} that encapsulates common base64, signing and encryption calls and operations in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @author Misagh Moayyed
 * @since 5.0.0
 */

@Slf4j
@UtilityClass
public class EncodingUtils {

    /**
     * JSON web key parameter that identifies the key..
     */
    public static final String JSON_WEB_KEY = "k";

    private static final Base32 BASE32_CHUNKED_ENCODER = new Base32(76, new byte[]{10});
    private static final Base32 BASE32_UNCHUNKED_ENCODER = new Base32(0, new byte[]{10});

    private static final Base64 BASE64_CHUNKED_ENCODER = new Base64(76, new byte[]{10});
    private static final Base64 BASE64_UNCHUNKED_ENCODER = new Base64(0, new byte[]{10});

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
     * Hex decode string.
     *
     * @param data the data
     * @return the string
     */
    public static String hexDecode(final char[] data) {
        try {
            val result = Hex.decodeHex(data);
            return new String(result, StandardCharsets.UTF_8);
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
    public static String hexEncode(final String data) {
        try {
            val result = Hex.encodeHex(data.getBytes(StandardCharsets.UTF_8));
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
            val result = Hex.encodeHex(data);
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
    public static String encodeUrlSafeBase64(final byte[] data) {
        return Base64.encodeBase64URLSafeString(data);
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the encoded array
     */
    public static byte[] decodeUrlSafeBase64(final String data) {
        return decodeBase64(data);
    }

    /**
     * Base64-encode the given byte[] as a string.
     *
     * @param data the byte array to encode
     * @return the encoded string
     */
    public static String encodeBase64(final byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * Base64-encode the given byte[] as a string.
     *
     * @param data    the byte array to encode
     * @param chunked the chunked
     * @return the encoded string
     */
    public static String encodeBase64(final byte[] data, final boolean chunked) {
        if (data != null && data.length > 0) {
            if (chunked) {
                return BASE64_CHUNKED_ENCODER.encodeToString(data).trim();
            }
            return BASE64_UNCHUNKED_ENCODER.encodeToString(data).trim();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Base64-encode the given string as a string.
     *
     * @param data the String to encode
     * @return the encoded string
     */
    public static String encodeBase64(final String data) {
        return Base64.encodeBase64String(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the decoded array
     */
    public static byte[] decodeBase64(final String data) {
        return Base64.decodeBase64(data);
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the decoded array
     */
    public static byte[] decodeBase64(final byte[] data) {
        return Base64.decodeBase64(data);
    }

    /**
     * Base64-decode the given string as String.
     *
     * @param data the base64 string
     * @return the string
     */
    public static String decodeBase64ToString(final String data) {
        return new String(decodeBase64(data), StandardCharsets.UTF_8);
    }

    /**
     * Base64-encode the given byte[] as a byte[].
     *
     * @param data the byte array to encode
     * @return the byte[] in base64
     */
    public static byte[] encodeBase64ToByteArray(final byte[] data) {
        return Base64.encodeBase64(data);
    }

    /**
     * Base32-encode the given byte[] as a string.
     *
     * @param data    the byte array to encode
     * @param chunked the chunked
     * @return the encoded string
     */
    public static String encodeBase32(final byte[] data, final boolean chunked) {
        if (chunked) {
            return BASE32_CHUNKED_ENCODER.encodeToString(data).trim();
        }
        return BASE32_UNCHUNKED_ENCODER.encodeToString(data).trim();
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
    @SneakyThrows
    public static String urlEncode(final String value, final String encoding) {
        return URLEncoder.encode(value, encoding);
    }

    /**
     * Url decode a value.
     *
     * @param value the value to decode
     * @return the decoded value
     */
    @SneakyThrows
    public static String urlDecode(final String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    }


    /**
     * Validates Base64 encoding.
     *
     * @param value the value to check
     * @return true if the string is validly Base64 encoded
     */
    public static boolean isBase64(final String value) {
        return Base64.isBase64(value);
    }

    /**
     * Verify jws signature byte [ ].
     *
     * @param signingKey the signing key
     * @param asString   the as string
     * @return the byte [ ]
     */
    @SneakyThrows
    public static byte[] verifyJwsSignature(final Key signingKey, final String asString) {
        val jws = new JsonWebSignature();
        jws.setCompactSerialization(asString);
        jws.setKey(signingKey);

        val verified = jws.verifySignature();
        if (verified) {
            val payload = jws.getEncodedPayload();
            LOGGER.trace("Successfully decoded value. Result in Base64-encoding is [{}]", payload);
            return EncodingUtils.decodeBase64(payload);
        }
        return null;
    }

    /**
     * Verify jws signature byte [ ].
     *
     * @param value      the value
     * @param signingKey the signing key
     * @return the byte [ ]
     */
    @SneakyThrows
    public static byte[] verifyJwsSignature(final Key signingKey, final byte[] value) {
        val asString = new String(value, StandardCharsets.UTF_8);
        return verifyJwsSignature(signingKey, asString);
    }


    /**
     * Generate octet json web key of given size .
     *
     * @param size the size
     * @return the key
     */
    public static String generateJsonWebKey(final int size) {
        val octetKey = OctJwkGenerator.generateJwk(size);
        val params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        return params.get(JSON_WEB_KEY).toString();
    }

    /**
     * Prepare json web token key.
     *
     * @param secret the secret
     * @return the key
     */
    @SneakyThrows
    public static Key generateJsonWebKey(final String secret) {
        val keys = new HashMap<String, Object>(2);
        keys.put("kty", "oct");
        keys.put(EncodingUtils.JSON_WEB_KEY, secret);
        val jwk = JsonWebKey.Factory.newJwk(keys);
        return jwk.getKey();
    }

    /**
     * Sign jws.
     *
     * @param key   the key
     * @param value the value
     * @return the byte []
     */
    public static byte[] signJwsHMACSha512(final Key key, final byte[] value) {
        return signJws(key, value, AlgorithmIdentifiers.HMAC_SHA512);
    }

    /**
     * Sign jws hmac sha 256.
     *
     * @param key   the key
     * @param value the value
     * @return the byte [ ]
     */
    public static byte[] signJwsHMACSha256(final Key key, final byte[] value) {
        return signJws(key, value, AlgorithmIdentifiers.HMAC_SHA256);
    }

    /**
     * Sign jws.
     *
     * @param key   the key
     * @param value the value
     * @return the byte []
     */
    public static byte[] signJwsRSASha512(final Key key, final byte[] value) {
        return signJws(key, value, AlgorithmIdentifiers.RSA_USING_SHA512);
    }

    /**
     * Sign jws.
     *
     * @param key            the key
     * @param value          the value
     * @param algHeaderValue the alg header value
     * @return the byte [ ]
     */
    @SneakyThrows
    public static byte[] signJws(final Key key, final byte[] value, final String algHeaderValue) {
        val base64 = EncodingUtils.encodeBase64(value);
        val jws = new JsonWebSignature();
        jws.setEncodedPayload(base64);
        jws.setAlgorithmHeaderValue(algHeaderValue);
        jws.setKey(key);
        jws.setHeader("typ", "JWT");
        return jws.getCompactSerialization().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Encrypt value as jwt with direct algorithm and encryption content alg aes-128-sha-256.
     *
     * @param key   the key
     * @param value the value
     * @return the string
     */
    public static String encryptValueAsJwtDirectAes128Sha256(final Key key, final Serializable value) {
        return encryptValueAsJwt(key, value, KeyManagementAlgorithmIdentifiers.DIRECT,
            CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
    }

    /**
     * Encrypt value as jwt rsa oeap 256 aes 256 sha 512 string.
     *
     * @param key   the key
     * @param value the value
     * @return the string
     */
    public static String encryptValueAsJwtRsaOeap256Aes256Sha512(final Key key, final Serializable value) {
        return encryptValueAsJwt(key, value, KeyManagementAlgorithmIdentifiers.RSA_OAEP_256,
            CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM);
    }

    /**
     * Encrypt the value based on the seed array whose length was given during afterPropertiesSet,
     * and the key and content encryption ids.
     *
     * @param secretKeyEncryptionKey               the secret key encryption key
     * @param value                                the value
     * @param algorithmHeaderValue                 the algorithm header value
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     * @return the encoded value
     */
    public static String encryptValueAsJwt(final Key secretKeyEncryptionKey,
                                           final Serializable value,
                                           final String algorithmHeaderValue,
                                           final String contentEncryptionAlgorithmIdentifier) {
        try {
            val jwe = new JsonWebEncryption();
            jwe.setPayload(value.toString());
            jwe.enableDefaultCompression();
            jwe.setAlgorithmHeaderValue(algorithmHeaderValue);
            jwe.setEncryptionMethodHeaderParameter(contentEncryptionAlgorithmIdentifier);
            jwe.setKey(secretKeyEncryptionKey);
            jwe.setHeader("typ", "JWT");
            LOGGER.trace("Encrypting via [{}]", contentEncryptionAlgorithmIdentifier);
            return jwe.getCompactSerialization();
        } catch (final JoseException e) {
            throw new IllegalArgumentException("Is JCE Unlimited Strength Jurisdiction Policy installed? " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt value based on the key created.
     *
     * @param secretKeyEncryptionKey the secret key encryption key
     * @param value                  the value
     * @return the decrypted value
     */
    @SneakyThrows
    public static String decryptJwtValue(final Key secretKeyEncryptionKey, final String value) {
        val jwe = new JsonWebEncryption();
        jwe.setKey(secretKeyEncryptionKey);
        jwe.setCompactSerialization(value);
        LOGGER.trace("Decrypting value...");
        try {
            return jwe.getPayload();
        } catch (final JoseException e) {
            if (LOGGER.isTraceEnabled()) {
                throw new DecryptionException(e);
            }
            throw new DecryptionException();
        }
    }

    /**
     * Is jce installed ?
     *
     * @return the boolean
     */
    public static boolean isJceInstalled() {
        try {
            val maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            return maxKeyLen == Integer.MAX_VALUE;
        } catch (final NoSuchAlgorithmException e) {
            return false;
        }
    }
}
