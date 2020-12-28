package org.apereo.cas.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SocketUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Utility")
public class SocketUtilsTests {
    @Test
    public void verifyExec() {
        assertTrue(SocketUtils.isTcpPortAvailable(1234));
    }
}
