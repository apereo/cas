package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Groovy")
class GroovyScriptInterruptInquirerTests {
    @Test
    void verifyResponseCanBeFoundFromGroovy() throws Throwable {
        val q = new GroovyScriptInterruptInquirer(new ClassPathResource("interrupt.groovy"));
        val response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            MockRequestContext.create());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
    }
}
