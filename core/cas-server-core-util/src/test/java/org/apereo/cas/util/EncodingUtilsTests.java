package org.apereo.cas.util;

import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;

/**
 * This is {@link EncodingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class EncodingUtilsTests {

    @SneakyThrows
    private static PrivateKey getPrivateKey() {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setLocation(new ClassPathResource("keys/RSA2048Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getPublicKey() {
        val factory = new PublicKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setResource(new ClassPathResource("keys/RSA2048Public.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @Test
    public void verifyAesKeyForJwtSigning() {
        val secret = EncodingUtils.generateJsonWebKey(512);
        val key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
        val value = "ThisValue";
        val signed = EncodingUtils.signJwsHMACSha512(key, value.getBytes(StandardCharsets.UTF_8));
        val jwt = EncodingUtils.verifyJwsSignature(key, signed);
        val result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtSigning() {
        val value = "ThisValue";
        val signed = EncodingUtils.signJwsRSASha512(getPrivateKey(), value.getBytes(StandardCharsets.UTF_8));
        val jwt = EncodingUtils.verifyJwsSignature(getPublicKey(), signed);
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
    }

    @Test
    public void verifyRsaKeyForJwtEncryption() {
        val value = "ThisValue";
        val found = EncodingUtils.encryptValueAsJwtRsaOeap256Aes256Sha512(getPublicKey(), value);
        val jwt = EncodingUtils.decryptJwtValue(getPrivateKey(), found);
        assertTrue(jwt.equals(value));
    }
}
