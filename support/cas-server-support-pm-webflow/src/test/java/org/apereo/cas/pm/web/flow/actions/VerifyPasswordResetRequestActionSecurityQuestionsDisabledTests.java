package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VerifyPasswordResetRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
@TestPropertySource(properties = "cas.authn.pm.reset.security-questions-enabled=false")
public class VerifyPasswordResetRequestActionSecurityQuestionsDisabledTests extends BasePasswordManagementActionTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("1.2.3.4");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val token = passwordManagementService.createToken("casuser");
        val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val serverPrefix = casProperties.getServer().getPrefix();
        val service = webApplicationServiceFactory.createService(serverPrefix);
        val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
        val ticket = transientFactory.create(service, properties);
        this.ticketRegistry.addTicket(ticket);
        request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(VerifyPasswordResetRequestAction.EVENT_ID_SECURITY_QUESTIONS_DISABLED,
            verifyPasswordResetRequestAction.execute(context).getId());
    }

}
