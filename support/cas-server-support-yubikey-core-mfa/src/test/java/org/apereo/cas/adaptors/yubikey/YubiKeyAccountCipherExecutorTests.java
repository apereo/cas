package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAccountCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Cipher")
class YubiKeyAccountCipherExecutorTests {
    @Test
    void verifyAction() {
        val cipher = new YubikeyAccountCipherExecutor(null, null,
            EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, 512,
            EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        val value = UUID.randomUUID().toString();
        val encoded = cipher.encode(value);
        assertEquals(value, cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }
}
