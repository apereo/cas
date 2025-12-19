package org.apereo.cas.audit.spi.resource;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LogoutRequestResourceResolverTests}.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@Tag("Audits")
class LogoutRequestResourceResolverTests {

    private static final String TGT_ID = "TGT-0";

    @Test
    void verifyLogoutRequest() {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
        when(jp.getArgs()).thenReturn(new Object[]{SingleLogoutExecutionRequest.builder().ticketGrantingTicket(tgt).build()});
        assertEquals(TGT_ID, resolver.resolveFrom(jp, new Object())[0]);
    }

    @Test
    void verifyException() {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
        when(jp.getArgs()).thenReturn(new Object[]{SingleLogoutExecutionRequest.builder().ticketGrantingTicket(tgt).build()});
        assertEquals(TGT_ID, resolver.resolveFrom(jp, new AuthenticationException())[0]);
    }

    private static AuditResourceResolver getResolver() {
        return new LogoutRequestResourceResolver();
    }
}
