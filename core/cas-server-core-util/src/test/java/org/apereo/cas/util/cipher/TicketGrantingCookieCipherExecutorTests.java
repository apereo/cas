package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.DecryptionException;
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

    @Test
    void verifyEncryptionWithoutSigning() {
        val crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();
        crypto.setSigningEnabled(false);
        val cipher = CipherExecutorUtils.newStringCipherExecutor(crypto, TicketGrantingCookieCipherExecutor.class);
        val value = "TGT-" + RandomUtils.randomAlphanumeric(2000);
        val encoded = cipher.encode(value);
        assertEquals(5, encoded.split("\\.", -1).length);
        assertEquals(value, cipher.decode(encoded));

        val signedAndEncrypted = CipherExecutorUtils.newStringCipherExecutor(
            new EncryptionOptionalSigningOptionalJwtCryptographyProperties(), TicketGrantingCookieCipherExecutor.class);
        assertTrue(encoded.length() * 5 < signedAndEncrypted.encode(value).length() * 4);

        val parts = encoded.split("\\.", -1);
        val ciphertext = parts[3];
        val middle = ciphertext.length() / 2;
        parts[3] = ciphertext.substring(0, middle) + (ciphertext.charAt(middle) == 'A' ? 'B' : 'A') + ciphertext.substring(middle + 1);
        assertThrows(DecryptionException.class, () -> cipher.decode(String.join(".", parts)));
    }

    @Test
    void verifySigningStaysEnabledWithSigningKey() {
        val crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();
        crypto.setSigningEnabled(false);
        crypto.getSigning().setKey("szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w");
        val cipher = CipherExecutorUtils.newStringCipherExecutor(crypto, TicketGrantingCookieCipherExecutor.class);
        val encoded = cipher.encode("CAS Test");
        assertEquals(3, encoded.split("\\.", -1).length);
        assertEquals("CAS Test", cipher.decode(encoded));
    }
}
