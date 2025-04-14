package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceAccessEnforcementAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Audits")
class ServiceAccessEnforcementAuditResourceResolverTests {

    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.audit.engine.audit-format=JSON")
    @ExtendWith(CasTestExtension.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("serviceAccessEnforcementAuditResourceResolver")
        protected AuditResourceResolver serviceAccessEnforcementAuditResourceResolver;
    }

    @Nested
    @TestPropertySource(properties = "cas.audit.engine.include-validation-assertion=false")
    class WithoutAssertionTests extends BaseTests {
        @Test
        void verifyAction() {
            val result = AuditableExecutionResult.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .authentication(CoreAuthenticationTestUtils.getAuthentication())
                .build();
            val outcome = serviceAccessEnforcementAuditResourceResolver.resolveFrom(mock(JoinPoint.class), result);
            assertTrue(outcome.length > 0);
        }
    }
    
    @Nested
    @TestPropertySource(properties = "cas.audit.engine.include-validation-assertion=true")
    class WithAssertionTests extends BaseTests {
        @Test
        void verifyAction() {
            val result = AuditableExecutionResult.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .authentication(CoreAuthenticationTestUtils.getAuthentication())
                .build();
            val outcome = serviceAccessEnforcementAuditResourceResolver.resolveFrom(mock(JoinPoint.class), result);
            assertTrue(outcome.length > 0);
        }
    }
}
