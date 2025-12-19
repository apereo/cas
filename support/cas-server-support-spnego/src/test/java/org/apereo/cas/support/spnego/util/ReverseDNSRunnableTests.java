package org.apereo.cas.support.spnego.util;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ReverseDNSRunnableTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
class ReverseDNSRunnableTests {
    @Test
    void verifyOperation() {
        val input = new ReverseDNSRunnable("123.456.000.xyz");
        input.run();
        assertNotNull(input.getHostName());
        assertNotNull(input.getIpAddress());
    }
}
