package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ValidatePasswordResetTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
public class ValidatePasswordResetTokenActionTests extends BasePasswordManagementActionTests {
    @Test
    public void verifyInvalidTickeeditpt() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, UUID.randomUUID().toString());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    public void verifyNoParam() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertNull(validatePasswordResetTokenAction.execute(context));
    }

    @Test
    public void verifyInvalidToken() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val ticketId = UUID.randomUUID().toString();
        val sts = mock(TransientSessionTicket.class);
        when(sts.getProperties()).thenReturn(Map.of(PasswordManagementService.PARAMETER_TOKEN, "invalid"));
        when(sts.getId()).thenReturn(ticketId);
        ticketRegistry.addTicket(sts);

        request.addParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticketId);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    public void verifyDoChangeWithValidSession() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        request.addParameter(PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, validatePasswordResetTokenAction.execute(context).getId());
    }
}
