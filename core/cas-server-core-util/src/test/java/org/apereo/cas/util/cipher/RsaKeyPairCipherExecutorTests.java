package org.apereo.cas.util.cipher;

import lombok.val;
import org.junit.jupiter.api.Test;

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
        val secretKeyEncryption = "classpath:keys/RSA2048Public.key";
        val secretKeySigning = "classpath:keys/RSA2048Private.key";
        val cipher1 = new TicketGrantingCookieCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        assertNotNull(cipher1.encode("TestValue"));

        val cipher = new ProtocolTicketCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        assertNotNull(cipher.encode("TestValue"));
    }

    @Test
    public void verifyRsaKeyPairResource() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey, privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPair() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        val cipher = new RsaKeyPairCipherExecutor(kp, kp);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairSigning() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        val cipher = new RsaKeyPairCipherExecutor(kp);
        val testValue = cipher.encode("Value");
        assertEquals("Value", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairSigningOnly() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyRsaKeyPairDoesNothing() {
        val cipher = new RsaKeyPairCipherExecutor();
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }
}
