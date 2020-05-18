package org.apereo.cas.util;

import org.apereo.cas.util.crypto.DecryptionException;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EncodingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
public class EncodingUtilsTests {

    @SneakyThrows
    private static PrivateKey getRSAPrivateKey() {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setLocation(new ClassPathResource("keys/RSA2048Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getRSAPublicKey() {
        val factory = new PublicKeyFactoryBean(new ClassPathResource("keys/RSA2048Public.key"), RsaKeyUtil.RSA);
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PrivateKey getP256PrivateKey() {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(EcKeyUtil.EC);
        factory.setLocation(new ClassPathResource("keys/ECsecp256r1Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getP256PublicKey() {
        val factory = new PublicKeyFactoryBean(new ClassPathResource("keys/ECsecp256r1Public.key"), EcKeyUtil.EC);
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PrivateKey getP384PrivateKey() {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(EcKeyUtil.EC);
        factory.setLocation(new ClassPathResource("keys/ECsecp384r1Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getP384PublicKey() {
        val factory = new PublicKeyFactoryBean(new ClassPathResource("keys/ECsecp384r1Public.key"), EcKeyUtil.EC);
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PrivateKey getP521PrivateKey() {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(EcKeyUtil.EC);
        factory.setLocation(new ClassPathResource("keys/ECsecp521r1Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getP521PublicKey() {
        val factory = new PublicKeyFactoryBean(new ClassPathResource("keys/ECsecp521r1Public.key"), EcKeyUtil.EC);
        factory.setSingleton(false);
        return factory.getObject();
    }

    @Test
    public void verifyAesKeyForJwtSigning() {
        val secret = EncodingUtils.generateJsonWebKey(512);
        val key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
        val value = "ThisValue";
        val signed = EncodingUtils.SIGN_JWS_HMAC_SHA512.signJws(key, value.getBytes(StandardCharsets.UTF_8), Map.of());
        val jwt = EncodingUtils.verifyJwsSignature(key, signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtSigning() {
        val value = "ThisValue";
        val signed = EncodingUtils.SIGN_JWS_RSA_SHA512.signJws(getRSAPrivateKey(), value.getBytes(StandardCharsets.UTF_8), Map.of());
        val jwt = EncodingUtils.verifyJwsSignature(getRSAPublicKey(), signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyP256KeyForJwtSigning() {
        val value = "ThisValue";
        val signed = EncodingUtils.SIGN_JWS_EC_P256.signJws(getP256PrivateKey(), value.getBytes(StandardCharsets.UTF_8), Map.of());
        val jwt = EncodingUtils.verifyJwsSignature(getP256PublicKey(), signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyP384KeyForJwtSigning() {
        val value = "ThisValue";
        val signed = EncodingUtils.SIGN_JWS_EC_P384.signJws(getP384PrivateKey(), value.getBytes(StandardCharsets.UTF_8), Map.of());
        val jwt = EncodingUtils.verifyJwsSignature(getP384PublicKey(), signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyP521KeyForJwtSigning() {
        val value = "ThisValue";
        val signed = EncodingUtils.SIGN_JWS_EC_P521.signJws(getP521PrivateKey(), value.getBytes(StandardCharsets.UTF_8), Map.of());
        val jwt = EncodingUtils.verifyJwsSignature(getP521PublicKey(), signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyAesKeyForJwtEncryption() {
        val secret = EncodingUtils.generateJsonWebKey(256);
        val key = EncodingUtils.generateJsonWebKey(secret);
        val value = "ThisValue";
        val found = EncodingUtils.encryptValueAsJwtDirectAes128Sha256(key, value);
        val jwt = EncodingUtils.decryptJwtValue(key, found);
        assertTrue(jwt.equals(value));

        assertThrows(DecryptionException.class, () -> EncodingUtils.decryptJwtValue(key, null));
    }
    

    @Test
    public void verifyRsaKeyForJwtEncryption() {
        val value = "ThisValue";
        val found = EncodingUtils.encryptValueAsJwtRsaOeap256Aes256Sha512(getRSAPublicKey(), value);
        val jwt = EncodingUtils.decryptJwtValue(getRSAPrivateKey(), found);
        assertTrue(jwt.equals(value));
    }

    @Test
    public void verifyHex() {
        assertNull(EncodingUtils.hexDecode("one"));
        assertNull(EncodingUtils.hexDecode(StringUtils.EMPTY));
        assertNull(EncodingUtils.hexEncode((byte[]) null));
        assertNull(EncodingUtils.hexEncode((String) null));
        assertNull(EncodingUtils.hexDecode("one".toCharArray()));
        assertNull(EncodingUtils.hexDecode((char[]) null));
    }

    @Test
    public void verifyEncoding() {
        assertTrue(EncodingUtils.encodeBase64(ArrayUtils.EMPTY_BYTE_ARRAY, true).isEmpty());
        assertFalse(EncodingUtils.encodeBase64("one".getBytes(StandardCharsets.UTF_8), true).isEmpty());

        assertFalse(EncodingUtils.encodeBase32("one".getBytes(StandardCharsets.UTF_8), true).isEmpty());
        assertFalse(EncodingUtils.encodeBase64("one".getBytes(StandardCharsets.UTF_8), false).isEmpty());
    }
}
