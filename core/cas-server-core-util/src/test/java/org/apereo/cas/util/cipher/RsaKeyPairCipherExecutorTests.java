package org.apereo.cas.util.cipher;

import org.apereo.cas.CipherExecutor;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

/**
 * This is {@link RsaKeyPairCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RsaKeyPairCipherExecutorTests {
    @Test
    public void verifyActionOneWay() {
        final String secretKeyEncryption = "classpath:keys/RSA2048Public.key";
        final String secretKeySigning = "classpath:keys/RSA2048Private.key";
        CipherExecutor cipher = new TicketGrantingCookieCipherExecutor(secretKeyEncryption, secretKeySigning);
        assertNotNull(cipher.encode("TestValue"));

        cipher = new ProtocolTicketCipherExecutor(secretKeyEncryption, secretKeySigning);
        assertNotNull(cipher.encode("TestValue"));
    }

    @Test
    public void verifyRsaKeyPairResource() {
        final String publicKey = "classpath:keys/RSA2048Public.key";
        final String privateKey = "classpath:keys/RSA2048Private.key";
        final CipherExecutor cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey, privateKey, publicKey);
        final Object testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPair() {
        final String publicKey = "classpath:keys/RSA2048Public.key";
        final String privateKey = "classpath:keys/RSA2048Private.key";
        final KeyPair kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        final CipherExecutor cipher = new RsaKeyPairCipherExecutor(kp, kp);
        final Object testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairSigning() {
        final String publicKey = "classpath:keys/RSA2048Public.key";
        final String privateKey = "classpath:keys/RSA2048Private.key";
        final KeyPair kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        final CipherExecutor cipher = new RsaKeyPairCipherExecutor(kp);
        final Object testValue = cipher.encode("Value");
        assertEquals("Value", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairSigningOnly() {
        final String publicKey = "classpath:keys/RSA2048Public.key";
        final String privateKey = "classpath:keys/RSA2048Private.key";
        final CipherExecutor cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey);
        final Object testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairDoesNothing() {
        final CipherExecutor cipher = new RsaKeyPairCipherExecutor();
        final Object testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }
}
