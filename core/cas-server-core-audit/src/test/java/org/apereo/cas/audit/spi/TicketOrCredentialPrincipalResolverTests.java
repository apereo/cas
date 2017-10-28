package org.apereo.cas.audit.spi;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TicketOrCredentialPrincipalResolver}
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class TicketOrCredentialPrincipalResolverTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyResolverByUnknownUser() {
        final TicketOrCredentialPrincipalResolver res =
                new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        assertEquals(res.resolve(), PrincipalResolver.UNKNOWN_USER);
    }

    @Test
    public void verifyResolverCredential() {
        final TicketOrCredentialPrincipalResolver res =
                new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);

        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        when(jp.getArgs()).thenReturn(new Object[] {c});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.toString());
    }

    @Test
    public void verifyResolverServiceTicket() {
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), c);

        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket st = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
                CoreAuthenticationTestUtils.getService(), ctx);

        final TicketOrCredentialPrincipalResolver res = new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);

        when(jp.getArgs()).thenReturn(new Object[] {st.getId()});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }

    @Test
    public void verifyResolverTicketGrantingTicket() {
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), c);

        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket st = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), CoreAuthenticationTestUtils.getService(), ctx);

        final TicketOrCredentialPrincipalResolver res = new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);

        when(jp.getArgs()).thenReturn(new Object[] {ticketId.getId()});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }


}
