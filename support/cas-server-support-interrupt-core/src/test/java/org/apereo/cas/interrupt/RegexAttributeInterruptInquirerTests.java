package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link RegexAttributeInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegexAttributeInterruptInquirerTests {
    @Test
    public void verifyResponseCanBeFoundFromAttributes() {
        final var q =
            new RegexAttributeInterruptInquirer("member..", "CA.|system");
        final var response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertTrue(response.isInterrupt());
    }
}
