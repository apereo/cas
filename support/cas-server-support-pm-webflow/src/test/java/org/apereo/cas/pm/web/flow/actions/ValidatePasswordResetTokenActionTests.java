package org.apereo.cas.pm.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
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

    private static final String TOKEN = "token";

    @BeforeEach
    void onSetUp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        val clientInfo = ClientInfo.from(request);
        ClientInfoHolder.setClientInfo(clientInfo);
    }

    @Test
    void verifyInvalidTicket() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, UUID.randomUUID().toString());
        assertEquals(CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    void verifyGoodTicket() throws Throwable {
        val query = PasswordManagementQuery.builder()
                .username("casuser").email("casuser@email.com").build();
        val token = passwordManagementService.createToken(query);

        val id = UUID.randomUUID().toString();
        val ticket = new TransientSessionTicketImpl(id, NeverExpiresExpirationPolicy.INSTANCE,
                CoreAuthenticationTestUtils.getService(), Map.of(TOKEN, token));
        ticketRegistry.addTicket(ticket);

        val context = MockRequestContext.create(applicationContext);
        context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticket.getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, validatePasswordResetTokenAction.execute(context).getId());
    }

    @Test
    void verifyNoParam() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertNull(validatePasswordResetTokenAction.execute(context));
    }

    @Test
    void verifyInvalidToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

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
        val context = MockRequestContext.create(applicationContext);
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        context.setParameter(PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD, "true");
        assertEquals(CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, validatePasswordResetTokenAction.execute(context).getId());
    }
}
