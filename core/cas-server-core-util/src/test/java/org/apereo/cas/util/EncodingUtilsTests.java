package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EncodingUtilsTests {

    @Test
    public void verifyAesKeyForJwtSigning() {
        final var secret = EncodingUtils.generateJsonWebKey(512);
        final Key key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
        final var value = "ThisValue";
        final var signed = EncodingUtils.signJwsHMACSha512(key, value.getBytes(StandardCharsets.UTF_8));
        final var jwt = EncodingUtils.verifyJwsSignature(key, signed);
        final var result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtSigning() {
        final var value = "ThisValue";
        final var signed = EncodingUtils.signJwsRSASha512(getPrivateKey(), value.getBytes(StandardCharsets.UTF_8));
        final var jwt = EncodingUtils.verifyJwsSignature(getPublicKey(), signed);
        final var result = new String(jwt, StandardCharsets.UTF_8);
        assertTrue(result.equals(value));
    }

    @Test
    public void verifyAesKeyForJwtEncryption() {
        final var secret = EncodingUtils.generateJsonWebKey(256);
        final var key = EncodingUtils.generateJsonWebKey(secret);
        final var value = "ThisValue";
        final var found = EncodingUtils.encryptValueAsJwtDirectAes128Sha256(key, value);
        final var jwt = EncodingUtils.decryptJwtValue(key, found);
        assertTrue(jwt.equals(value));
    }

    @Test
    public void verifyRsaKeyForJwtEncryption() {
        final var value = "ThisValue";
        final var found = EncodingUtils.encryptValueAsJwtRsaOeap256Aes256Sha512(getPublicKey(), value);
        final var jwt = EncodingUtils.decryptJwtValue(getPrivateKey(), found);
        assertTrue(jwt.equals(value));
    }

    @SneakyThrows
    private static PrivateKey getPrivateKey() {
        final var factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setLocation(new ClassPathResource("keys/RSA2048Private.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }

    @SneakyThrows
    private static PublicKey getPublicKey() {
        final var factory = new PublicKeyFactoryBean();
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setResource(new ClassPathResource("keys/RSA2048Public.key"));
        factory.setSingleton(false);
        return factory.getObject();
    }
}
