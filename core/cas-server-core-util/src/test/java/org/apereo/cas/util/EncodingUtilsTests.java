package org.apereo.cas.util;

import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.Key;
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

    @Test
    public void verifyAesKeyForJwtSigning() {
        final String secret = EncodingUtils.generateJsonWebKey(512);
        final Key key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
        final String value = "ThisValue";
        final byte[] signed = EncodingUtils.signJwsHMACSha512(key, value.getBytes(StandardCharsets.UTF_8));
        final byte[] jwt = EncodingUtils.verifyJwsSignature(key, signed);
        final String result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtSigning() {
        final String value = "ThisValue";
        final byte[] signed = EncodingUtils.signJwsRSASha512(getPrivateKey(), value.getBytes(StandardCharsets.UTF_8));
        final byte[] jwt = EncodingUtils.verifyJwsSignature(getPublicKey(), signed);
        final String result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyAesKeyForJwtEncryption() {
        final String secret = EncodingUtils.generateJsonWebKey(256);
        final Key key = EncodingUtils.generateJsonWebKey(secret);
        final String value = "ThisValue";
        final String found = EncodingUtils.encryptValueAsJwtDirectAes128Sha256(key, value);
        final String jwt = EncodingUtils.decryptJwtValue(key, found);
        assertTrue(jwt.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtEncryption() {
        final String value = "ThisValue";
        final String found = EncodingUtils.encryptValueAsJwtRsaOeap256Aes256Sha512(getPublicKey(), value);
        final String jwt = EncodingUtils.decryptJwtValue(getPrivateKey(), found);
        assertTrue(jwt.equals(value));
    }
    
    private static PrivateKey getPrivateKey() {
        try {
            final PrivateKeyFactoryBean factory = new PrivateKeyFactoryBean();
            factory.setAlgorithm(RsaKeyUtil.RSA);
            factory.setLocation(new ClassPathResource("keys/RSA2048Private.key"));
            factory.setSingleton(false);
            return factory.getObject();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getPublicKey() {
        try {
            final PublicKeyFactoryBean factory = new PublicKeyFactoryBean();
            factory.setAlgorithm(RsaKeyUtil.RSA);
            factory.setLocation(new ClassPathResource("keys/RSA2048Public.key"));
            factory.setSingleton(false);
            return factory.getObject();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
