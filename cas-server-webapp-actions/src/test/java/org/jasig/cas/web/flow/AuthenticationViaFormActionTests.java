package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.validation.constraints.NotNull;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class AuthenticationViaFormActionTests extends AbstractCentralAuthenticationServiceTests {

    private AuthenticationViaFormAction action;

    private CookieGenerator warnCookieGenerator;

    @Before
    public void onSetUp() throws Exception {
        this.action = new AuthenticationViaFormAction();

        this.warnCookieGenerator = new CookieGenerator();
        this.warnCookieGenerator.setCookieName("WARN");
        this.warnCookieGenerator.setCookieName("TGT");
        this.warnCookieGenerator.setCookieDomain("/");
        this.warnCookieGenerator.setCookiePath("/");

        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setAuthenticationSystemSupport(getAuthenticationSystemSupport());
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");
        request.addParameter("username", "test");
        request.addParameter("password", "test");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoServiceAndWarn()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, response));
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifySuccessfulAuthenticationWithServiceAndWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");
        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");
        request.addParameter("service", "test");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,  response));
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifyFailedAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));

        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(c, "credentials"));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("error", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndSameCredentials() throws Exception {
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        final Service service = TestUtils.getService(TestUtils.CONST_TEST_URL);
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils.getAuthenticationContext(
                getAuthenticationSystemSupport(), service, c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        request.addParameter("renew", "true");
        request.addParameter("service", TestUtils.getService(TestUtils.CONST_TEST_URL).getId());
        request.addParameter("username", "test");
        request.addParameter("password", "test");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put("service", TestUtils.getService());

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("warn", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndDifferentCredentials() throws Exception {
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();

        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils.getAuthenticationContext(
                getAuthenticationSystemSupport(), TestUtils.getService("test"), c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", TestUtils.getService("test").getId());
        request.addParameter("username", "test2");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndBadCredentials() throws Exception {
        final Credential c = org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword();
        final Service service = TestUtils.getService("test");
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils.getAuthenticationContext(
                getAuthenticationSystemSupport(), service, c);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", service.getId());

        final Credential c2 = org.jasig.cas.authentication.TestUtils.getCredentialsWithDifferentUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);
        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(c2, "credentials"));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("error", this.action.submit(context, c2, messageContext).getId());
    }


    /**
     * Put credentials in request scope.
     *
     * @param context the context
     * @param c the credential
     */
    private static void putCredentialInRequestScope(
            final RequestContext context, @NotNull final Credential c) {
        context.getRequestScope().put("credentials", c);
    }
}
