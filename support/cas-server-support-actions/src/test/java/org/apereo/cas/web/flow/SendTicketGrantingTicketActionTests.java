package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
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
            context.setRemoteAddr(LOCALHOST_IP);
            context.setLocalAddr(LOCALHOST_IP);
            context.getHttpServletRequest().addParameter(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION, "true");
            context.setClientInfo();
            context.withUserAgent();

            val tgt1 = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            getTicketRegistry().addTicket(tgt1);
            WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
            WebUtils.putTicketGrantingTicketIntoMap(context.getRequestScope(), tgt1.getId());

            val tgt2 = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            getTicketRegistry().addTicket(tgt2);
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
            context.setRemoteAddr(LOCALHOST_IP);
            context.setLocalAddr(LOCALHOST_IP);
            context.setClientInfo();

            context.withUserAgent();
            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            getTicketRegistry().addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            context.setRequestCookiesFromResponse();
            assertEquals(tgt.getId(), getTicketGrantingTicketCookieGenerator().retrieveCookieValue(context.getHttpServletRequest()));
        }

        @Test
        void verifyTgtToSetRemovingOldTgt() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr(LOCALHOST_IP);
            context.setLocalAddr(LOCALHOST_IP);
            context.setClientInfo();
            context.withUserAgent();

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            getTicketRegistry().addTicket(tgt);
            context.setHttpRequestCookies(new Cookie("TGT", "test5"));
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
            context.setRemoteAddr(LOCALHOST_IP);
            context.setLocalAddr(LOCALHOST_IP);
            context.withUserAgent();
            context.setClientInfo();

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            context.setHttpRequestCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(0, context.getHttpServletResponse().getCookies().length);
        }

        @Test
        void verifySsoSessionCookieOnServiceSsoDisallowed() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setRemoteAddr(LOCALHOST_IP);
            context.setLocalAddr(LOCALHOST_IP);
            context.withUserAgent();
            context.setClientInfo();

            val svc = mock(WebApplicationService.class);
            when(svc.getId()).thenReturn("TestSsoFalse");

            val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
            context.setHttpRequestCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, svc);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(0, context.getHttpServletResponse().getCookies().length);
        }
    }
}
