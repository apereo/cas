package org.apereo.cas.qr.authentication;

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
public class QRAuthenticationTokenCredentialTests {
    @Test
    public void verifyOperation() {
        val c = new QRAuthenticationTokenCredential("token");
        assertNotNull(c.getId());
        assertNotNull(c.toString());
    }

}
