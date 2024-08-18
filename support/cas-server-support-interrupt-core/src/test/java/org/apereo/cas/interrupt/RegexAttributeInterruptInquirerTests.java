package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegexAttributeInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Attributes")
class RegexAttributeInterruptInquirerTests {
    @Test
    void verifyResponseCanBeFoundFromAttributes() throws Throwable {
        val inquirer = new RegexAttributeInterruptInquirer("member..", "CA.|system");
        val response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertTrue(response.isInterrupt());
    }

    @Test
    void verifyInterruptSkippedWithServicePolicy() throws Throwable {
        val inquirer = new RegexAttributeInterruptInquirer("member..", "CA.|system");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setWebflowInterruptPolicy(new DefaultRegisteredServiceWebflowInterruptPolicy().setEnabled(false));
        val response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            registeredService,
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isInterrupt());
    }
}
