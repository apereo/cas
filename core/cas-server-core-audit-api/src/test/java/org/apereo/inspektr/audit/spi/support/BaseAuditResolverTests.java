package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import lombok.val;
import module java.base;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseAuditResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
public abstract class BaseAuditResolverTests {
    protected static Audit getAuditAnnotation() {
        val audit = mock(Audit.class);
        when(audit.action()).thenReturn("ACTION");
        return audit;
    }

    protected void verifyAuditActionResolver(final AuditActionResolver resolver, final Audit audit) {
        assertNotNull(resolver.resolveFrom(mock(JoinPoint.class), false, audit));
        assertNotNull(resolver.resolveFrom(mock(JoinPoint.class), new RuntimeException(), audit));
    }

    protected void verifyAuditResourceResolver(final AuditResourceResolver resolver, final JoinPoint jp,
                                               final Object returnValue) {
        assertTrue(resolver.resolveFrom(jp, returnValue).length > 0);
        assertTrue(resolver.resolveFrom(jp, new RuntimeException("Error")).length > 0);
    }
    
}
