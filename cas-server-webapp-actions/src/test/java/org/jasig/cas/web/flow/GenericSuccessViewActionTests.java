package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GenericSuccessViewActionTests {

    @Test
    public void verifyValidPrincipal() throws InvalidTicketException {
        final CentralAuthenticationService cas = mock(CentralAuthenticationService.class);
        final Authentication authn = mock(Authentication.class);
        when(authn.getPrincipal()).thenReturn(
                org.jasig.cas.authentication.TestUtils.getPrincipal("cas"));
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authn);



        when(cas.getTicket(any(String.class), any(Ticket.class.getClass()))).thenReturn(tgt);
        final GenericSuccessViewAction action = new GenericSuccessViewAction(cas);
        final Principal p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertEquals(p.getId(), "cas");
    }

    @Test
    public void verifyPrincipalCanNotBeDetemined() throws InvalidTicketException {
        final CentralAuthenticationService cas = mock(CentralAuthenticationService.class);
        when(cas.getTicket(any(String.class), any(Ticket.class.getClass()))).thenThrow(new InvalidTicketException("TGT-1"));
        final GenericSuccessViewAction action = new GenericSuccessViewAction(cas);
        final Principal p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertTrue(p instanceof NullPrincipal);
    }
}
