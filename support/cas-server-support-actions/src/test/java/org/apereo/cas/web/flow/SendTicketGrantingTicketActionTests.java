package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@Tag("WebflowActions")
class SendTicketGrantingTicketActionTests {
    private static final String LOCALHOST_IP = "127.0.0.1";
    @Nested
    class PublicWorkstationCookie extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;
        @Test
        void verifyTgtMismatch() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletRequest().setRemoteAddr(LOCALHOST_IP);
            context.getHttpServletRequest().setLocalAddr(LOCALHOST_IP);
            context.getHttpServletRequest().addParameter(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION, "true");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            context.addHeader("User-Agent", "Test");

            val tgt1 = new MockTicketGrantingTicket(UUID.randomUUID().toString());

            WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
            WebUtils.putTicketGrantingTicketIntoMap(context.getRequestScope(), tgt1.getId());

            val tgt2 = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            WebUtils.putTicketGrantingTicketIntoMap(context.getFlowScope(), tgt2.getId());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        }

    }

    @Nested
    class CreateSsoCookieOnRenew extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;

        
        @Test
        void verifyNoTgtToSet() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        }

        @Test
        void verifyTgtToSet() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletRequest().setRemoteAddr(LOCALHOST_IP);
            context.getHttpServletRequest().setLocalAddr(LOCALHOST_IP);
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            context.addHeader("User-Agent", "Test");
            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            context.setRequestCookiesFromResponse();
            assertEquals(tgt.getId(), getTicketGrantingTicketCookieGenerator().retrieveCookieValue(context.getHttpServletRequest()));
        }

        @Test
        void verifyTgtToSetRemovingOldTgt() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletRequest().setRemoteAddr(LOCALHOST_IP);
            context.getHttpServletRequest().setLocalAddr(LOCALHOST_IP);
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            context.addHeader("User-Agent", "Test");

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            context.getHttpServletRequest().setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            context.setRequestCookiesFromResponse();
            assertEquals(tgt.getId(), getTicketGrantingTicketCookieGenerator().retrieveCookieValue(context.getHttpServletRequest()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.sso.create-sso-cookie-on-renew-authn=false")
    class IgnoreSsoCookieOnRenew extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;

        @Test
        void verifySsoSessionCookieOnRenewAsParameter() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            
            context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
            context.getHttpServletRequest().setRemoteAddr(LOCALHOST_IP);
            context.getHttpServletRequest().setLocalAddr(LOCALHOST_IP);
            context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            context.getHttpServletRequest().setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(0, context.getHttpServletResponse().getCookies().length);
        }

        @Test
        void verifySsoSessionCookieOnServiceSsoDisallowed() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.getHttpServletRequest().setRemoteAddr(LOCALHOST_IP);
            context.getHttpServletRequest().setLocalAddr(LOCALHOST_IP);
            context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

            val svc = mock(WebApplicationService.class);
            when(svc.getId()).thenReturn("TestSsoFalse");

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            context.getHttpServletRequest().setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, svc);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(0, context.getHttpServletResponse().getCookies().length);
        }
    }
}
