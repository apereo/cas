package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.TestUtils;
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
import org.apereo.cas.web.support.WebUtils;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasSupportActionsConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
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
public class GenerateServiceTicketActionTests extends AbstractCentralAuthenticationServiceTests {

    @Autowired
    @Qualifier("generateServiceTicketAction")
    private Action action;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void onSetUp() throws Exception {
        final AuthenticationResult authnResult =
                getAuthenticationSystemSupport()
                        .handleAndFinalizeSingleAuthenticationTransaction(TestUtils.getService(),
                                TestUtils.getCredentialsWithSameUsernameAndPassword());

        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(authnResult);
        getTicketRegistry().addTicket(this.ticketGrantingTicket);
    }

    @Test
    public void verifyServiceTicketFromCookie() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.apereo.cas.services.TestUtils.getService());
        context.getFlowScope().put("ticketGrantingTicketId", this.ticketGrantingTicket.getId());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service");
        request.setCookies(new Cookie("TGT", this.ticketGrantingTicket.getId()));

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketFromRequest() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.apereo.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service");
        WebUtils.putTicketGrantingTicketInScopes(context,
                this.ticketGrantingTicket);

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.apereo.cas.services.TestUtils.getService());
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyTicketGrantingTicketExpiredTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.apereo.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service");
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.ticketGrantingTicket.markTicketExpired();
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }
    
    @Test
    public void verifyTicketGrantingTicketNotTgtButGateway() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.apereo.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service");
        request.addParameter(CasProtocolConstants.PARAMETER_GATEWAY, "true");
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.STATE_ID_GATEWAY, this.action.execute(context).getId());
    }
}
