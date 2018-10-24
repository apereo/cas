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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@TestPropertySource(properties = "cas.sso.createSsoCookieOnRenewAuthn=false")
public class SendTicketGrantingTicketActionSsoTests extends AbstractWebflowActionsTests {

    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String TEST_STRING = "test";
    private static final String SUCCESS = "success";

    @Autowired
    @Qualifier("sendTicketGrantingTicketAction")
    private Action action;

    private MockRequestContext context;

    @BeforeEach
    public void onSetUp() {
        this.context = new MockRequestContext();
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
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        assertEquals(SUCCESS, action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }

    @Test
    public void verifySsoSessionCookieOnServiceSsoDisallowed() throws Exception {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val svc = mock(WebApplicationService.class);
        when(svc.getId()).thenReturn("TestSsoFalse");

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TEST_STRING);
        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        this.context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, svc);

        assertEquals(SUCCESS, action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }
}
