package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class AuthenticationViaFormActionTests extends AbstractWebflowActionsTests {

    private static final String TEST = "test";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";

    @Autowired
    @Qualifier("authenticationViaFormAction")
    private Action action;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    /**
     * Put credentials in request scope.
     *
     * @param context the context
     * @param c       the credential
     */
    private static void putCredentialInRequestScope(final RequestContext context, final Credential c) {
        context.getRequestScope().put("credential", c);
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoService() throws Exception {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoServiceAndWarn() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);
        request.addParameter("warn", "true");

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulAuthenticationWithServiceAndWarn() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, TEST);
        request.addParameter("warn", "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifyFailedAuthenticationWithNoService() throws Exception {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();

        request.addParameter(USERNAME_PARAM, TEST);
        request.addParameter(PASSWORD_PARAM, "test2");

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        context.getRequestScope().put("org.springframework.validation.BindException.credentials", new BindException(c, "credential"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyRenewWithServiceAndSameCredentials() throws Exception {
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), service, c);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
            RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL).getId());
        putCredentialInRequestScope(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService());

        val ev = this.action.execute(context);
        assertEquals(CasWebflowConstants.STATE_ID_WARN, ev.getId());
    }

    @Test
    public void verifyRenewWithServiceAndDifferentCredentials() throws Exception {
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();

        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), RegisteredServiceTestUtils.getService(TEST), c);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(TEST).getId());

        val c2 = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifyRenewWithServiceAndBadCredentials() throws Exception {
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val service = RegisteredServiceTestUtils.getService(TEST);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(
            getAuthenticationSystemSupport(), service, c);

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val c2 = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }
}
