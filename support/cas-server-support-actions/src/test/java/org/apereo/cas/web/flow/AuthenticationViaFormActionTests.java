package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.validation.BindException;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@DirtiesContext
@Import(CasSupportActionsConfiguration.class)
public class AuthenticationViaFormActionTests extends AbstractCentralAuthenticationServiceTests {

    private static final String TEST = "test";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";

    @Autowired
    @Qualifier("authenticationViaFormAction")
    private Action action;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Test
    public void verifySuccessfulAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoServiceAndWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);
        request.addParameter("warn", "true");

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifySuccessfulAuthenticationWithServiceAndWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);
        request.addParameter("warn", "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifyFailedAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, "test2");

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        context.getRequestScope().put("org.springframework.validation.BindException.credentials", new BindException(c, "credential"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyRenewWithServiceAndSameCredentials() throws Exception {
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Service service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL);
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
                getAuthenticationSystemSupport(), service, c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL).getId());
        putCredentialInRequestScope(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService());

        final Event ev = this.action.execute(context);
        assertEquals(CasWebflowConstants.STATE_ID_SUCCESS, ev.getId());
    }

    @Test
    public void verifyRenewWithServiceAndDifferentCredentials() throws Exception {
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
                getAuthenticationSystemSupport(), RegisteredServiceTestUtils.getService(TEST), c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(TEST).getId());

        final Credential c2 = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifyRenewWithServiceAndBadCredentials() throws Exception {
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Service service = RegisteredServiceTestUtils.getService(TEST);
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
                getAuthenticationSystemSupport(), service, c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        final Credential c2 = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    /**
     * Put credentials in request scope.
     *
     * @param context the context
     * @param c       the credential
     */
    private static void putCredentialInRequestScope(final RequestContext context, final Credential c) {
        context.getRequestScope().put("credential", c);
    }
}
