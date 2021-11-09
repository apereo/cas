package org.apereo.cas.webauthn;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class WebAuthnCredentialTests {
    @Test
    public void verifyOperation() {
        val c1 = new WebAuthnCredential(UUID.randomUUID().toString());
        assertNotNull(c1.getToken());
        val c2 = new WebAuthnCredential();
        c2.setToken(c1.getToken());
        assertEquals(c1, c2);
    }

}
