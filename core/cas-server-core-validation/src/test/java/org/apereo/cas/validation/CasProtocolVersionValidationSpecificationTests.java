package org.apereo.cas.validation;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CoreValidationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
public class CasProtocolVersionValidationSpecificationTests extends BaseCasCoreTests {
    @Test
    public void verifyOperation() throws Exception {
        val spec = new CasProtocolVersionValidationSpecification(
            Set.of(CasProtocolValidationSpecification.CasProtocolVersions.CAS10));
        val registeredService = mock(RegisteredService.class);
        val assertion = CoreValidationTestUtils.getAssertion(registeredService);
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }

    @Test
    public void verifySupportOperations() {
        val spec = new CasProtocolVersionValidationSpecification(
            Set.of(CasProtocolValidationSpecification.CasProtocolVersions.CAS10,
                CasProtocolValidationSpecification.CasProtocolVersions.CAS20));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getSupportedProtocols()).thenReturn(
            Set.of(CasProtocolValidationSpecification.CasProtocolVersions.CAS20.name()));

        val assertion = CoreValidationTestUtils.getAssertion(registeredService);
        assertFalse(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));

        when(registeredService.getSupportedProtocols()).thenReturn(
            Set.of(CasProtocolValidationSpecification.CasProtocolVersions.CAS10.name(),
                CasProtocolValidationSpecification.CasProtocolVersions.CAS20.name()));
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));

        when(registeredService.getSupportedProtocols()).thenReturn(Set.of());
        assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }
}
