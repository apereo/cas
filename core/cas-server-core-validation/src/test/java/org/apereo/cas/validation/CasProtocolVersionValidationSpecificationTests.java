package org.apereo.cas.validation;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CoreValidationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.CasProtocolVersions;
import org.apereo.cas.services.RegisteredService;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
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
class CasProtocolVersionValidationSpecificationTests {
    @Nested
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class TenantTests extends BaseCasCoreTests {
        @Test
        void verifyOperation() {
            val spec = new CasProtocolVersionValidationSpecification(
                Set.of(CasProtocolVersions.SAML1), tenantExtractor);
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            val assertion = CoreValidationTestUtils.getAssertion(registeredService);

            val request = new MockHttpServletRequest();
            request.setContextPath("/tenants/shire/login");
            
            assertTrue(spec.isSatisfiedBy(assertion, request));
        }
    }
    
    @Nested
    class DefaultTests extends BaseCasCoreTests {
        @Test
        void verifyOperation() {
            val spec = new CasProtocolVersionValidationSpecification(
                Set.of(CasProtocolVersions.CAS10), tenantExtractor);
            val registeredService = mock(RegisteredService.class);
            val assertion = CoreValidationTestUtils.getAssertion(registeredService);
            assertTrue(spec.isSatisfiedBy(assertion, new MockHttpServletRequest()));
        }

        @Test
        void verifySupportOperations() {
            val request = new MockHttpServletRequest();
            val protocolVersions = Set.of(CasProtocolVersions.CAS10, CasProtocolVersions.CAS20);

            val spec = new CasProtocolVersionValidationSpecification(protocolVersions, tenantExtractor);
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getSupportedProtocols()).thenReturn(Set.of(CasProtocolVersions.CAS20));

            val assertion = CoreValidationTestUtils.getAssertion(registeredService);
            assertFalse(spec.isSatisfiedBy(assertion, request));

            when(registeredService.getSupportedProtocols()).thenReturn(protocolVersions);
            assertTrue(spec.isSatisfiedBy(assertion, request));

            when(registeredService.getSupportedProtocols()).thenReturn(Set.of());
            assertTrue(spec.isSatisfiedBy(assertion, request));
        }
    }
}
