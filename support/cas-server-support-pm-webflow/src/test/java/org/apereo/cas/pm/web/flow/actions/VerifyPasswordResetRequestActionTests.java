package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VerifyPasswordResetRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
public class VerifyPasswordResetRequestActionTests {

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @TestPropertySource(properties = "cas.ticket.tst.number-of-uses=2")
    public class PasswordResetTokenMultiUse extends BasePasswordManagementActionTests {
        @Test
        public void verifyAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());

            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordResetRequestAction.execute(context).getId());
            assertNotNull(centralAuthenticationService.getTicket(ticket.getId()));
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @TestPropertySource(properties = "cas.authn.pm.reset.security-questions-enabled=false")
    public class SecurityQuestionsDisabled extends BasePasswordManagementActionTests {
        @Test
        public void verifyAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
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

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class SecurityQuestionsEnabled extends BasePasswordManagementActionTests {
        @Test
        public void verifyInvalidToken() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, UUID.randomUUID().toString());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }

        @Test
        public void verifyAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());

            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordResetRequestAction.execute(context).getId());

            assertTrue(PasswordManagementWebflowUtils.isPasswordResetSecurityQuestionsEnabled(context));
            assertNotNull(PasswordManagementWebflowUtils.getPasswordResetUsername(context));
            assertNotNull(PasswordManagementWebflowUtils.getPasswordResetToken(context));
            assertThrows(InvalidTicketException.class, () -> centralAuthenticationService.getTicket(ticket.getId()));
        }

        @Test
        public void verifyNoQuestionsAvailAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("noquestions").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }

        @Test
        public void verifyBadTicketAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, "badticket");
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }
    }
}
