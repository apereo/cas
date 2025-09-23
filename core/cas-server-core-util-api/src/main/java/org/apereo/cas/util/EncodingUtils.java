package org.apereo.cas.util;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.util.crypto.DecryptionException;
import org.apereo.cas.util.crypto.IdentifiableKey;
import org.apereo.cas.util.jwt.JsonWebTokenEncryptor;
import org.apereo.cas.util.jwt.JsonWebTokenSigner;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link EncodingUtils} that encapsulates common base64,
 * signing and encryption calls and operations in one spot.
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
            LOGGER.trace(e.getMessage(), e);
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
            LOGGER.trace(e.getMessage(), e);
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
            LOGGER.trace(e.getMessage(), e);
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
     * Encode url safe base64 string.
     *
     * @param data the data
     * @return the string
     */
    public static String encodeUrlSafeBase64(final String data) {
        return Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8));
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
    public static String urlEncode(final String value, final String encoding) {
        return Unchecked.supplier(() -> URLEncoder.encode(value, encoding)).get();
    }

    /**
     * Url decode a value.
     *
     * @param value the value to decode
     * @return the decoded value
     */
    public static String urlDecode(final String value) {
        return StringUtils.isBlank(value)
            ? value
            : Unchecked.supplier(() -> URLDecoder.decode(value, StandardCharsets.UTF_8)).get();
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
    public static byte[] verifyJwsSignature(final Key signingKey, final String asString) {
        return Unchecked.supplier(() -> {
            val jws = new JsonWebSignature();
            jws.setCompactSerialization(asString);
            jws.setKey(signingKey);

            val verified = jws.verifySignature();
            if (verified) {
                val payload = jws.getEncodedPayload();
                LOGGER.trace("Successfully decoded value. Result in Base64url-encoding is [{}]", payload);
                return EncodingUtils.decodeUrlSafeBase64(payload);
            }
            return null;
        }).get();
    }

    /**
     * Verify jws signature byte [ ].
     *
     * @param value      the value
     * @param signingKey the signing key
     * @return the byte [ ]
     */
    public static byte[] verifyJwsSignature(final Key signingKey, final byte[] value) {
        val asString = new String(value, StandardCharsets.UTF_8);
        return verifyJwsSignature(signingKey, asString);
    }


    /**
     * Is json web key boolean.
     *
     * @param key the key
     * @return true/false
     */
    public boolean isJsonWebKey(final String key) {
        try {
            val results = parseJsonWebKey(key);
            return !results.isEmpty();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage());
        }
        return false;
    }

    /**
     * New Json web key.
     *
     * @param json the json
     * @return the json web key
     */
    public static JsonWebKey newJsonWebKey(final String json) {
        return Unchecked.supplier(() -> JsonWebKey.Factory.newJwk(json)).get();
    }

    /**
     * New Json web key.
     *
     * @param size the size
     * @return the json web key
     */
    public static JsonWebKey newJsonWebKey(final int size) {
        return Unchecked.supplier(() -> RsaJwkGenerator.generateJwk(size, null, RandomUtils.getNativeInstance())).get();
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
    public static Key generateJsonWebKey(final String secret) {
        val keys = new HashMap<String, Object>(2);
        keys.put("kty", "oct");
        keys.put(EncodingUtils.JSON_WEB_KEY, secret);
        return generateJsonWebKey(keys);
    }

    /**
     * Generate json web key.
     *
     * @param params the params
     * @return the key
     */
    public static Key generateJsonWebKey(final Map<String, Object> params) {
        return Unchecked.supplier(() -> {
            val jwk = JsonWebKey.Factory.newJwk(params);
            return jwk.getKey();
        }).get();
    }


    /**
     * Sign jws.
     *
     * @param key     the key
     * @param value   the value
     * @param headers the headers
     * @return the byte []
     */
    public static byte[] signJwsHMACSha512(final Key key, final byte[] value, final Map<String, Object> headers) {
        return JsonWebTokenSigner.builder()
            .key(key)
            .headers(headers)
            .algorithm(AlgorithmIdentifiers.HMAC_SHA512)
            .build()
            .sign(value);
    }

    /**
     * Sign jws.
     *
     * @param key     the key
     * @param value   the value
     * @param headers the headers
     * @return the byte []
     */
    public static byte[] signJwsRSASha512(final Key key, final byte[] value, final Map<String, Object> headers) {
        return JsonWebTokenSigner.builder()
            .key(key)
            .headers(headers)
            .algorithm(AlgorithmIdentifiers.RSA_USING_SHA512)
            .build()
            .sign(value);
    }

    /**
     * Encrypt value as jwt with direct algorithm and encryption content alg aes-128-sha-256.
     *
     * @param key   the key
     * @param value the value
     * @return the string
     */
    public static String encryptValueAsJwtDirectAes128Sha256(final Key key, final Serializable value) {
        return JsonWebTokenEncryptor.builder()
            .key(key)
            .algorithm(KeyManagementAlgorithmIdentifiers.DIRECT)
            .encryptionMethod(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256)
            .build()
            .encrypt(value);
    }

    /**
     * Encrypt value as jwt rsa oeap 256 aes 256 sha 512 string.
     *
     * @param key   the key
     * @param value the value
     * @return the string
     */
    public static String encryptValueAsJwtRsaOeap256Aes256Sha512(final Key key, final Serializable value) {
        return JsonWebTokenEncryptor.builder()
            .key(key)
            .algorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256)
            .encryptionMethod(EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM)
            .build()
            .encrypt(value);
    }

    /**
     * Decrypt value based on the key created.
     *
     * @param givenKey the secret key encryption key
     * @param value    the value
     * @return the decrypted value
     */
    public static String decryptJwtValue(final Key givenKey, final String value) {
        try {
            val realKey = givenKey instanceof final IdentifiableKey idk ? idk.getKey() : givenKey;
            val jwe = new JsonWebEncryption();
            jwe.setKey(realKey);
            jwe.setCompactSerialization(value);
            LOGGER.trace("Decrypting value...");
            return jwe.getPayload();
        } catch (final Exception e) {
            if (LOGGER.isTraceEnabled()) {
                throw new DecryptionException(e);
            }
            throw new DecryptionException();
        }
    }

    /**
     * Parse json web key map.
     *
     * @param key the key
     * @return the map
     * @throws Exception the exception
     */
    public static Map<String, Object> parseJsonWebKey(final String key) throws Exception {
        return JsonUtil.parseJson(key);
    }
}
