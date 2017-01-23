package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasSupportActionsConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreConfiguration.class,
        CasPersonDirectoryAttributeRepositoryConfiguration.class,
        CasCoreUtilConfiguration.class})
@DirtiesContext
public class SendTicketGrantingTicketActionTests extends AbstractCentralAuthenticationServiceTests {

    @Autowired
    @Qualifier("sendTicketGrantingTicketAction")
    private SendTicketGrantingTicketAction action;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    private MockRequestContext context;

    @Before
    public void onSetUp() throws Exception {
        this.context = new MockRequestContext();
    }

    @Test
    public void verifyNoTgtToSet() throws Exception {
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals("success", this.action.execute(this.context).getId());
    }

    @Test
    public void verifyTgtToSet() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("User-Agent", "Test");
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");

        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                request, response));

        assertEquals("success", this.action.execute(this.context).getId());
        request.setCookies(response.getCookies());
        assertEquals(tgt.getId(), this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request));
    }

    @Test
    public void verifyTgtToSetRemovingOldTgt() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setLocalAddr("127.0.0.1");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("User-Agent", "Test");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");

        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        assertEquals("success", this.action.execute(this.context).getId());
        request.setCookies(response.getCookies());
        assertEquals(tgt.getId(), this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request));
    }

    @Test
    public void verifySsoSessionCookieOnRenewAsParameter() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");
        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        this.action.setCreateSsoSessionCookieOnRenewAuthentications(false);
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }

    @Test
    public void verifySsoSessionCookieOnServiceSsoDisallowed() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final WebApplicationService svc = mock(WebApplicationService.class);
        when(svc.getId()).thenReturn("TestSsoFalse");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");
        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        this.context.getFlowScope().put("service", svc);
        this.action.setCreateSsoSessionCookieOnRenewAuthentications(false);
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }
}
