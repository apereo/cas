package org.apereo.cas.util.cipher;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebflowConversationStateCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WebflowConversationStateCipherExecutorTests {
    @Test
    public void verifyAction() {
        val cipher = new WebflowConversationStateCipherExecutor(null, null,
            "AES", 512, 16, "webflow");
        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }
}
