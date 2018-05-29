package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceAccessEnforcementAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ServiceAccessEnforcementAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        final var r = new ServiceAccessEnforcementAuditResourceResolver();
        final var result = AuditableExecutionResult.of(
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        final var outcome = r.resolveFrom(mock(JoinPoint.class), result);
        assertTrue(outcome.length > 0);
    }
}
