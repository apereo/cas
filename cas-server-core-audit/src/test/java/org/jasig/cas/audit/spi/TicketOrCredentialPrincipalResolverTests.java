package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.inspektr.common.spi.PrincipalResolver;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TicketOrCredentialPrincipalResolver}
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class TicketOrCredentialPrincipalResolverTests extends AbstractCentralAuthenticationServiceTest {

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

        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        when(jp.getArgs()).thenReturn(new Object[] {c});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.toString());
    }

    @Test
    public void verifyResolverServiceTicket() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
                .createTicketGrantingTicket(c);
        final ServiceTicket st = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
                TestUtils.getService());

        final TicketOrCredentialPrincipalResolver res =
                new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);

        when(jp.getArgs()).thenReturn(new Object[] {st.getId()});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }

    @Test
    public void verifyResolverTicketGrantingTicket() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
                .createTicketGrantingTicket(c);

        final TicketOrCredentialPrincipalResolver res =
                new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);

        when(jp.getArgs()).thenReturn(new Object[] {ticketId.getId()});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, c.getId());
    }

    @Test
    public void verifyResolverSecurityContext() throws Exception {
        final UserDetails ud = mock(UserDetails.class);
        when(ud.getUsername()).thenReturn("pid");
        final Authentication authn = mock(Authentication.class);
        when(authn.getPrincipal()).thenReturn(ud);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authn);
        SecurityContextHolder.setContext(securityContext);

        final TicketOrCredentialPrincipalResolver res =
                new TicketOrCredentialPrincipalResolver(getCentralAuthenticationService());
        final JoinPoint jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{ud});

        final String result = res.resolveFrom(jp, null);
        assertNotNull(result);
        assertEquals(result, ud.getUsername());
    }
}
