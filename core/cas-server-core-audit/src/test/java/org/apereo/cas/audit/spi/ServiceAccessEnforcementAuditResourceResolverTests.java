package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceAccessEnforcementAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Audits")
@SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.audit.engine.audit-format=JSON")
class ServiceAccessEnforcementAuditResourceResolverTests {
    @Autowired
    @Qualifier("serviceAccessEnforcementAuditResourceResolver")
    private AuditResourceResolver serviceAccessEnforcementAuditResourceResolver;

    @Test
    void verifyAction() throws Throwable {
        val result = AuditableExecutionResult.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(CoreAuthenticationTestUtils.getAuthentication())
            .build();
        val outcome = serviceAccessEnforcementAuditResourceResolver.resolveFrom(mock(JoinPoint.class), result);
        assertTrue(outcome.length > 0);
    }
}
