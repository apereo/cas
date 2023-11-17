package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketAsFirstParameterResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
public class TicketAsFirstParameterResourceResolverTests {
    @Test
    public void verifyOperation() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434"});
        val resolver = getResolver();
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    @Test
    public void verifyTicketWithService() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()});
        val resolver = getResolver();
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    private static TicketAsFirstParameterResourceResolver getResolver() {
        return new TicketAsFirstParameterResourceResolver(
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
    }

    @Test
    public void verifyTicketWithServiceAsJson() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()});
        val resolver = getResolver();
        resolver.setAuditFormat(AuditTrailManager.AuditFormats.JSON);
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    @Test
    public void verifyNullOperation() {
        val jp = mock(JoinPoint.class);
        val resolver = getResolver();
        val input = resolver.resolveFrom(jp, null);
        assertEquals(0, input.length);
    }
}
