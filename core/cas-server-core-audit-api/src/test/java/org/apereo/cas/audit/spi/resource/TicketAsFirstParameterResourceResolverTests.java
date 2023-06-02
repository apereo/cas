package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
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
        val resolver = new TicketAsFirstParameterResourceResolver(new AuditEngineProperties());
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    @Test
    public void verifyTicketWithService() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()});
        val resolver = new TicketAsFirstParameterResourceResolver(new AuditEngineProperties().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    @Test
    public void verifyTicketWithServiceAsJson() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()});
        val resolver = new TicketAsFirstParameterResourceResolver(new AuditEngineProperties().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
        val input = resolver.resolveFrom(jp, null);
        assertTrue(input.length > 0);
    }

    @Test
    public void verifyNullOperation() {
        val jp = mock(JoinPoint.class);
        val resolver = new TicketAsFirstParameterResourceResolver(new AuditEngineProperties());
        val input = resolver.resolveFrom(jp, null);
        assertEquals(0, input.length);
    }
}
