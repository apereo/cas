package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
@Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
class RegisteredServiceMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyOperationByNoPolicy() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val result = registeredServiceMultifactorAuthenticationTrigger.isActivated(
            CoreAuthenticationTestUtils.getAuthentication(), registeredService,
            new MockHttpServletRequest(), new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService());
        assertFalse(result.isPresent());
    }

    @Test
    void verifyBadInput() throws Throwable {
        val result = registeredServiceMultifactorAuthenticationTrigger.isActivated(null, null,
            new MockHttpServletRequest(), new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService());
        assertFalse(result.isPresent());
    }

    @Test
    void verifyOperationByPolicyForPrincipal() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of("mfa-dummy"));
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("mail");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn("@example.org");
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        val result = registeredServiceMultifactorAuthenticationTrigger.isActivated(
            CoreAuthenticationTestUtils.getAuthentication(), registeredService,
            new MockHttpServletRequest(), new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService());
        assertFalse(result.isPresent());
    }

    @Test
    void verifyOperationByProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val result = registeredServiceMultifactorAuthenticationTrigger.isActivated(
            CoreAuthenticationTestUtils.getAuthentication(), registeredService,
            new MockHttpServletRequest(), new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService());
        assertTrue(result.isPresent());
    }

    @Test
    void verifyOperationByNoKnownProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of("unknown"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        val result = registeredServiceMultifactorAuthenticationTrigger.isActivated(
            CoreAuthenticationTestUtils.getAuthentication(), registeredService,
            new MockHttpServletRequest(), new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService());
        assertTrue(result.isEmpty());
    }
}
