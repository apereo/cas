package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class ValidatePasswordResetTokenActionTests extends BasePasswordManagementActionTests {
    @Test
    void verifyInvalidTicket() throws Throwable {
        val context = MockRequestContext.create();
        context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, UUID.randomUUID().toString());
        assertEquals(CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    void verifyNoParam() throws Throwable {
        val context = MockRequestContext.create();
        assertNull(validatePasswordResetTokenAction.execute(context));
    }

    @Test
    void verifyInvalidToken() throws Throwable {
        val context = MockRequestContext.create();

        val ticketId = UUID.randomUUID().toString();
        val sts = mock(TransientSessionTicket.class);
        when(sts.getProperties()).thenReturn(Map.of(PasswordManagementService.PARAMETER_TOKEN, "invalid"));
        when(sts.getId()).thenReturn(ticketId);
        ticketRegistry.addTicket(sts);

        context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticketId);
        assertEquals(CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    void verifyDoChangeWithValidSession() throws Throwable {
        val context = MockRequestContext.create();
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        context.setParameter(PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD, "true");
        assertEquals(CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, validatePasswordResetTokenAction.execute(context).getId());
    }
}
