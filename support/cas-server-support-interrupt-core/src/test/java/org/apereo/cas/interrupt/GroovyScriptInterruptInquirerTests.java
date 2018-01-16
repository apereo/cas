package org.apereo.cas.interrupt;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyScriptInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyScriptInterruptInquirerTests {
    @Test
    public void verifyResponseCanBeFoundFromGroovy() {
        final GroovyScriptInterruptInquirer q = new GroovyScriptInterruptInquirer(new ClassPathResource("interrupt.groovy"));
        final InterruptResponse response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
                CoreAuthenticationTestUtils.getRegisteredService(),
                CoreAuthenticationTestUtils.getService());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
    }
}
