package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulSamlIdPMetadataCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Cipher")
class RestfulSamlIdPMetadataCipherExecutorTests {
    @Test
    void verifyAction() throws Throwable {
        val cipher = new RestfulSamlIdPMetadataCipherExecutor(null, null,
            EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM, 512, 256);

        val encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }
}
