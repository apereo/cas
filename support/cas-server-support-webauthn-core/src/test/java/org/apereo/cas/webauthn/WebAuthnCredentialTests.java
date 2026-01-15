package org.apereo.cas.webauthn;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
class WebAuthnCredentialTests {
    @Test
    void verifyOperation() {
        val c1 = new WebAuthnCredential(UUID.randomUUID().toString());
        assertNotNull(c1.getToken());
        val c2 = new WebAuthnCredential();
        c2.setToken(c1.getToken());
        assertEquals(c1, c2);
    }

}
