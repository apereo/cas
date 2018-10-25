package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GenericSuccessViewActionTests {

    @Test
    public void verifyValidPrincipal() throws InvalidTicketException {
        val cas = mock(CentralAuthenticationService.class);
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);

        val authn = mock(Authentication.class);
        when(authn.getPrincipal()).thenReturn(
            CoreAuthenticationTestUtils.getPrincipal("cas"));
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authn);

        when(cas.getTicket(any(String.class), any())).thenReturn(tgt);
        val action = new GenericSuccessViewAction(cas, mgr, factory, "");
        val p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertEquals("cas", p.getId());
    }

    @Test
    public void verifyPrincipalCanNotBeDetermined() throws InvalidTicketException {
        val cas = mock(CentralAuthenticationService.class);
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);
        when(cas.getTicket(any(String.class), any())).thenThrow(new InvalidTicketException("TGT-1"));
        val action = new GenericSuccessViewAction(cas, mgr, factory, "");
        val p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertTrue(p instanceof NullPrincipal);
    }
}
