package org.apereo.cas.util.cipher;

import module java.base;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RsaKeyPairCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Cipher")
class RsaKeyPairCipherExecutorTests {
    @Test
    void verifyActionOneWay() {
        val secretKeyEncryption = "classpath:keys/RSA2048Public.key";
        val secretKeySigning = "classpath:keys/RSA2048Private.key";
        val cipher1 = new TicketGrantingCookieCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        cipher1.setSigningAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        assertNotNull(cipher1.encode("TestValue"));

        val cipher = new ProtocolTicketCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        cipher.setSigningAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA384);
        assertNotNull(cipher.encode("TestValue"));
    }

    @Test
    void verifyRsaKeyPairResource() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey, privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    void verifyRsaKeyPair() {
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
    void verifyRsaKeyPairSigning() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        val cipher = new RsaKeyPairCipherExecutor(kp);
        val testValue = cipher.encode("Value");
        assertEquals("Value", cipher.decode(testValue));
    }

    @Test
    void verifyRsaKeyPairSigningOnly() {
        val publicKey = "classpath:keys/RSA2048Public.key";
        val privateKey = "classpath:keys/RSA2048Private.key";
        val cipher = new RsaKeyPairCipherExecutor(privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    void verifyRsaKeyPairDoesNothing() {
        val cipher = new RsaKeyPairCipherExecutor();
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }
}
