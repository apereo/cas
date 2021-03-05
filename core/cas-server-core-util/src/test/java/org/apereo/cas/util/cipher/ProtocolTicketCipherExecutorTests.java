package org.apereo.cas.util.cipher;

import org.apereo.cas.configuration.model.core.util.TicketProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ProtocolTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Tickets")
public class ProtocolTicketCipherExecutorTests {
    @Test
    public void verifyAction() {
        val cipher = new ProtocolTicketCipherExecutor();
        val encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    public void verifyOperation() {
        val crypto = new TicketProperties().getCrypto();
        val cipher = CipherExecutorUtils.newStringCipherExecutor(crypto, ProtocolTicketCipherExecutor.class);
        val encoded = cipher.encode("ST-1234567890");
        assertEquals("ST-1234567890", cipher.decode(encoded));
    }

}
