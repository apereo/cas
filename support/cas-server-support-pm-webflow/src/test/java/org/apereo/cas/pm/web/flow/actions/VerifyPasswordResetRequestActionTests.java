package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.io.Serializable;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VerifyPasswordResetRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class VerifyPasswordResetRequestActionTests {

    @Nested
    @TestPropertySource(properties = "cas.ticket.tst.number-of-uses=2")
    class PasswordResetTokenMultiUse extends BasePasswordManagementActionTests {
        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());

            context.getHttpServletRequest().setRemoteAddr("1.2.3.4");
            context.getHttpServletRequest().setLocalAddr("1.2.3.4");
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementService.PARAMETER_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticket.getId());
            
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordResetRequestAction.execute(context).getId());
            assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.reset.security-questions-enabled=false")
    class SecurityQuestionsDisabled extends BasePasswordManagementActionTests {
        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletRequest().setRemoteAddr("1.2.3.4");
            context.getHttpServletRequest().setLocalAddr("1.2.3.4");
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementService.PARAMETER_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticket.getId());
            
            assertEquals(VerifyPasswordResetRequestAction.EVENT_ID_SECURITY_QUESTIONS_DISABLED,
                verifyPasswordResetRequestAction.execute(context).getId());
        }
    }

    @Nested
    class SecurityQuestionsEnabled extends BasePasswordManagementActionTests {
        @Test
        void verifyInvalidToken() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            
            
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, UUID.randomUUID().toString());
            
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }

        @Test
        void verifyActionWithoutToken() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.getHttpServletRequest().setRemoteAddr("1.2.3.4");
            context.getHttpServletRequest().setLocalAddr("1.2.3.4");
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);

            
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordResetRequestAction.execute(context).getId());
        }

        @Test
        void verifyActionWithResetToken() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            
            
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());

            context.getHttpServletRequest().setRemoteAddr("1.2.3.4");
            context.getHttpServletRequest().setLocalAddr("1.2.3.4");
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementService.PARAMETER_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            ticketRegistry.addTicket(ticket);
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticket.getId());
            
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordResetRequestAction.execute(context).getId());

            assertTrue(PasswordManagementWebflowUtils.isPasswordResetSecurityQuestionsEnabled(context));
            assertNotNull(PasswordManagementWebflowUtils.getPasswordResetUsername(context));
            assertNotNull(PasswordManagementWebflowUtils.getPasswordResetToken(context));
            assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticket.getId(), TransientSessionTicket.class));
        }

        @Test
        void verifyNoQuestionsAvailAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            
            context.getHttpServletRequest().setRemoteAddr("1.2.3.4");
            context.getHttpServletRequest().setLocalAddr("1.2.3.4");
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
            
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("noquestions").build());
            val transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
            val serverPrefix = casProperties.getServer().getPrefix();
            val service = webApplicationServiceFactory.createService(serverPrefix);
            val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementService.PARAMETER_TOKEN, token);
            val ticket = transientFactory.create(service, properties);
            this.ticketRegistry.addTicket(ticket);
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, ticket.getId());
            
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }

        @Test
        void verifyBadTicketAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, "badticket");
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordResetRequestAction.execute(context).getId());
        }
    }
}
