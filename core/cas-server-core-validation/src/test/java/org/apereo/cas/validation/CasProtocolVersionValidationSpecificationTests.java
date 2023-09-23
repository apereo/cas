package org.apereo.cas.validation;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CoreValidationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.CasProtocolVersions;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasProtocolVersionValidationSpecificationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("CAS")
class CasProtocolVersionValidationSpecificationTests extends BaseCasCoreTests {
    @Test
    void verifyOperation() throws Throwable {
        val spec = new CasProtocolVersionValidationSpecification(
            Set.of(CasProtocolVersions.CAS10));
        val registeredService = mock(RegisteredService.class);
        val assertion = CoreValidationTestUtils.getAssertion(registeredService);
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }

    @Test
    void verifySupportOperations() throws Throwable {
        val spec = new CasProtocolVersionValidationSpecification(
            Set.of(CasProtocolVersions.CAS10, CasProtocolVersions.CAS20));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getSupportedProtocols()).thenReturn(
            Set.of(CasProtocolVersions.CAS20));

        val assertion = CoreValidationTestUtils.getAssertion(registeredService);
        assertFalse(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));

        when(registeredService.getSupportedProtocols()).thenReturn(
            Set.of(CasProtocolVersions.CAS10, CasProtocolVersions.CAS20));
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));

        when(registeredService.getSupportedProtocols()).thenReturn(Set.of());
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }
}
