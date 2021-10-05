package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class YubiKeyAccountCipherExecutorTests {
    @Test
    public void verifyAction() {
        val cipher = new YubikeyAccountCipherExecutor(null, null,
            CipherExecutor.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, 512, 256);

        val encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }
}
