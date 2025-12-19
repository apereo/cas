package org.apereo.cas.qr.authentication;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationTokenCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class QRAuthenticationTokenCredentialTests {
    @Test
    void verifyOperation() {
        val c = new QRAuthenticationTokenCredential("token", UUID.randomUUID().toString());
        assertNotNull(c.getId());
        assertNotNull(c.toString());
    }

    @Test
    void verifyCtor() {
        val c = new QRAuthenticationTokenCredential("token", UUID.randomUUID().toString());
        val c1 = new QRAuthenticationTokenCredential();
        c1.setId("token");
        c1.setDeviceId(c.getDeviceId());
        assertNotNull(c.getId());
        assertEquals(c1, c);
    }
}
