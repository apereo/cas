package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSSLContextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
public class CasSSLContextTests {
    @Test
    public void verifyOperation() {
        val system = CasSSLContext.system();
        assertNotNull(system.getSslContext());
        assertNotNull(system.getKeyManagers());
        assertNotNull(system.getTrustManagers());
    }
}
