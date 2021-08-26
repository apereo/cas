package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@Tag("WebflowActions")
public class SendTicketGrantingTicketActionTests {

    private static final String LOCALHOST_IP = "127.0.0.1";

    private static final String TEST_STRING = "test";

    private static final String SUCCESS = "success";

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class PublicWorkstationCookie extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;

        private MockRequestContext context;

        @BeforeEach
        public void onSetUp() {
            context = new MockRequestContext();
        }

        @Test
        public void verifyTgtMismatch() throws Exception {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr(LOCALHOST_IP);
            request.setLocalAddr(LOCALHOST_IP);
            request.addParameter(WebUtils.PUBLIC_WORKSTATION_ATTRIBUTE, "true");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val response = new MockHttpServletResponse();
            request.addHeader("User-Agent", "Test");

            val tgt1 = mock(TicketGrantingTicket.class);
            when(tgt1.getId()).thenReturn(TEST_STRING);

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
            WebUtils.putTicketGrantingTicketIntoMap(context.getRequestScope(), tgt1.getId());

            val tgt2 = mock(TicketGrantingTicket.class);
            when(tgt2.getId()).thenReturn(TEST_STRING + "--" + TEST_STRING);
            WebUtils.putTicketGrantingTicketIntoMap(context.getFlowScope(), tgt2.getId());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            assertEquals(SUCCESS, action.execute(context).getId());
        }

    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class CreateSsoCookieOnRenew extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;

        private MockRequestContext context;

        @BeforeEach
        public void onSetUp() {
            context = new MockRequestContext();
        }

        @Test
        public void verifyNoTgtToSet() throws Exception {
            context.setExternalContext(
                new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
            assertEquals(SUCCESS, action.execute(context).getId());
        }

        @Test
        public void verifyTgtToSet() throws Exception {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr(LOCALHOST_IP);
            request.setLocalAddr(LOCALHOST_IP);
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val response = new MockHttpServletResponse();
            request.addHeader("User-Agent", "Test");
            val tgt = mock(TicketGrantingTicket.class);
            when(tgt.getId()).thenReturn(TEST_STRING);

            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            assertEquals(SUCCESS, action.execute(context).getId());
            request.setCookies(response.getCookies());
            assertEquals(tgt.getId(), getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request));
        }

        @Test
        public void verifyTgtToSetRemovingOldTgt() throws Exception {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr(LOCALHOST_IP);
            request.setLocalAddr(LOCALHOST_IP);
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val response = new MockHttpServletResponse();
            request.addHeader("User-Agent", "Test");

            val tgt = mock(TicketGrantingTicket.class);
            when(tgt.getId()).thenReturn(TEST_STRING);

            request.setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            assertEquals(SUCCESS, action.execute(context).getId());
            request.setCookies(response.getCookies());
            assertEquals(tgt.getId(), getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.sso.create-sso-cookie-on-renew-authn=false")
    public class IgnoreSsoCookieOnRenew extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
        private Action action;
        
        private MockRequestContext context;

        @BeforeEach
        public void onSetUp() {
            context = new MockRequestContext();
        }
        
        @Test
        public void verifySsoSessionCookieOnRenewAsParameter() throws Exception {
            val response = new MockHttpServletResponse();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
            request.setRemoteAddr(LOCALHOST_IP);
            request.setLocalAddr(LOCALHOST_IP);
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val tgt = mock(TicketGrantingTicket.class);
            when(tgt.getId()).thenReturn(TEST_STRING);
            request.setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            assertEquals(SUCCESS, action.execute(context).getId());
            assertEquals(0, response.getCookies().length);
        }

        @Test
        public void verifySsoSessionCookieOnServiceSsoDisallowed() throws Exception {
            val response = new MockHttpServletResponse();
            val request = new MockHttpServletRequest();

            request.setRemoteAddr(LOCALHOST_IP);
            request.setLocalAddr(LOCALHOST_IP);
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val svc = mock(WebApplicationService.class);
            when(svc.getId()).thenReturn("TestSsoFalse");

            val tgt = mock(TicketGrantingTicket.class);
            when(tgt.getId()).thenReturn(TEST_STRING);
            request.setCookies(new Cookie("TGT", "test5"));
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, svc);

            assertEquals(SUCCESS, action.execute(context).getId());
            assertEquals(0, response.getCookies().length);
        }
    }
}
