package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyScriptInterruptInquirerTests {
    @Test
    public void verifyResponseCanBeFoundFromGroovy() {
        val q = new GroovyScriptInterruptInquirer(new ClassPathResource("interrupt.groovy"));
        val response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
    }
}
