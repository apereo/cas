package org.apereo.cas.util.cipher;

import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link BaseStringCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Cipher")
class TicketGrantingCookieCipherExecutorTests {

    @Test
    void verifyAction() {
        val cipher = new TicketGrantingCookieCipherExecutor();
        val encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    void checkEncryptionWithDefaultSettings() {
        val cipherExecutor = new TicketGrantingCookieCipherExecutor("1PbwSbnHeinpkZOSZjuSJ8yYpUrInm5aaV18J2Ar4rM",
            "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
            ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256, 0, 0);

        val result = cipherExecutor.decode(cipherExecutor.encode("CAS Test"));
        assertEquals("CAS Test", result);
    }
}
