package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.spi.resource.ServiceAccessEnforcementAuditResourceResolver;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
    @Test
    void verifyAction() throws Throwable {
        val r = new ServiceAccessEnforcementAuditResourceResolver(
            new AuditEngineProperties().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
        val result = AuditableExecutionResult.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(CoreAuthenticationTestUtils.getAuthentication())
            .build();
        val outcome = r.resolveFrom(mock(JoinPoint.class), result);
        assertTrue(outcome.length > 0);
    }
}
