package org.jasig.cas.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.DefaultAuthenticationAttributesPopulator;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTokenIdGenerator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.bind.support.DefaultSpringBindCredentialsBinder;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.view.RedirectView;

import junit.framework.TestCase;

public class LoginControllerTests extends TestCase {

    private LoginController loginController;

    private CentralAuthenticationServiceImpl centralAuthenticationService;

    private CentralAuthenticationService getCentralAuthenticationService() {
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl();
        this.centralAuthenticationService
            .setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketRegistry(new DefaultTicketRegistry());
        this.centralAuthenticationService
            .setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());

        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();

        List populators = new ArrayList();
        populators.add(new DefaultAuthenticationAttributesPopulator());

        List resolvers = new ArrayList();
        resolvers.add(new DefaultCredentialsToPrincipalResolver());
        resolvers.add(new HttpBasedServiceCredentialsToPrincipalResolver());

        List handlers = new ArrayList();
        handlers.add(new SimpleTestUsernamePasswordAuthenticationHandler());
        handlers.add(new HttpBasedServiceCredentialsAuthenticationHandler());

        manager.setAuthenticationAttributesPopulators(populators);
        manager.setAuthenticationHandlers(handlers);
        manager.setCredentialsToPrincipalResolvers(resolvers);

        this.centralAuthenticationService.setAuthenticationManager(manager);

        return this.centralAuthenticationService;
    }

    protected void setUp() throws Exception {
        this.loginController = new LoginController();
        this.loginController
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.loginController.setLoginTokens(new HashMap());
        this.loginController.afterPropertiesSet();

    }

    public void testAfterPropertiesSet() throws Exception {
        this.loginController
            .setCredentialsBinder(new DefaultSpringBindCredentialsBinder());
        this.loginController
            .setUniqueTokenIdGenerator(new DefaultUniqueTokenIdGenerator());
        this.loginController.setFormView("test");
        this.loginController.setSuccessView("test");

        this.loginController.setCommandClass(UsernamePasswordCredentials.class);
        this.loginController.afterPropertiesSet();
    }

    public void testAfterPropertiesSetWithBadCredentialsBinder()
        throws Exception {
        this.loginController.setCredentialsBinder(new CredentialsBinder(){

            public void bind(HttpServletRequest request, Credentials credentials) {
                return;
            }

            public boolean supports(Class clazz) {
                return false;
            }
        });
        this.loginController
            .setUniqueTokenIdGenerator(new DefaultUniqueTokenIdGenerator());
        this.loginController.setFormView("test");
        this.loginController.setSuccessView("test");

        this.loginController.setCommandClass(UsernamePasswordCredentials.class);

        try {
            this.loginController.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetNoLoginTokens() {
        this.loginController.setLoginTokens(null);
        try {
            this.loginController.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testUniqueLoginTokens() throws Exception {
        String loginToken1, loginToken2;

        loginToken1 = (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN);
        loginToken2 = (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN);
        assertNotNull(loginToken1);
        assertNotNull(loginToken2);
        assertNotSame(loginToken1, loginToken2);
    }

    public void testGatewayAndService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("gateway", "test");
        request.addParameter("service", "test");

        assertTrue(this.loginController
            .showForm(
                request,
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getView() instanceof RedirectView);
    }

    public void testRenewIsTrue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("renew", "true");

        assertNotNull(this.loginController.showForm(request,
            new MockHttpServletResponse(), new BindException(
                new UsernamePasswordCredentials(), "credentials")));
    }

    public void testTicketGrantingTicketAndService() throws Exception {
        String ticketGrantingTicketId;
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");

        ticketGrantingTicketId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);

        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            ticketGrantingTicketId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "Test");
        request.setCookies(new Cookie[] {cookie});

        assertTrue(this.loginController
            .showForm(
                request,
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getView() instanceof RedirectView);
    }

    public void testTicketGrantingTicketAndServiceWarn() throws Exception {
        String ticketGrantingTicketId;
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");

        ticketGrantingTicketId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);

        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            ticketGrantingTicketId);
        Cookie cookie2 = new Cookie(WebConstants.COOKIE_PRIVACY,
            WebConstants.COOKIE_DEFAULT_FILLED_VALUE);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "Test");
        request.setCookies(new Cookie[] {cookie, cookie2});

        assertEquals(ViewNames.CONST_LOGON_CONFIRM, this.loginController
            .showForm(
                request,
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getViewName());
    }

    public void testTicketGrantingTicketAndServiceWithRenew() throws Exception {
        String ticketGrantingTicketId;
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");

        ticketGrantingTicketId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);

        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            ticketGrantingTicketId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "Test");
        request.addParameter("renew", "true");
        request.setCookies(new Cookie[] {cookie});

        assertEquals(ViewNames.CONST_LOGON, this.loginController
            .showForm(
                request,
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getViewName());
    }

    public void testNoCredentials() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("loginToken", (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN));
        try {
            Credentials c = new UsernamePasswordCredentials();
            this.loginController.processFormSubmission(request,
                new MockHttpServletResponse(), c, new BindException(c,
                    "credentials"));
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidCredentialsNoOtherParams() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("userName", "test");
        request.addParameter("password", "test");
        request.addParameter("loginToken", (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN));

        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("test");
        c.setPassword("test");

        assertEquals(ViewNames.CONST_LOGON_SUCCESS, this.loginController
            .processFormSubmission(request, new MockHttpServletResponse(), c,
                new BindException(c, "credentials")).getViewName());
    }

    public void testValidCredentialsWithService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("userName", "test");
        request.addParameter("password", "test");
        request.addParameter("service", "test");
        request.addParameter("loginToken", (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN));

        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("test");
        c.setPassword("test");

        assertTrue(this.loginController.processFormSubmission(request,
            new MockHttpServletResponse(), c,
            new BindException(c, "credentials")).getView() instanceof RedirectView);
    }

    public void testValidCredentialsWithServiceAndWarn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("userName", "test");
        request.addParameter("password", "test");
        request.addParameter("service", "test");
        request.addParameter("warn", "test");
        request.addParameter("loginToken", (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN));

        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("test");
        c.setPassword("test");

        assertEquals(ViewNames.CONST_LOGON_CONFIRM, this.loginController
            .processFormSubmission(request, new MockHttpServletResponse(), c,
                new BindException(c, "credentials")).getViewName());
    }

    public void testValidCredentialsWithServiceAndRenew() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("userName", "test");
        request.addParameter("password", "test");
        request.addParameter("service", "test");
        request.addParameter("renew", "true");
        request.addParameter("loginToken", (String) this.loginController
            .showForm(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new BindException(new UsernamePasswordCredentials(),
                    "credentials")).getModel().get(WebConstants.LOGIN_TOKEN));

        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("test");
        c.setPassword("test");

        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            this.centralAuthenticationService.createTicketGrantingTicket(c));

        request.setCookies(new Cookie[] {cookie});

        assertTrue(this.loginController.processFormSubmission(request,
            new MockHttpServletResponse(), c,
            new BindException(c, "credentials")).getView() instanceof RedirectView);
    }

    public void testNoLoginToken() throws Exception {
        Credentials c = new UsernamePasswordCredentials();
        assertNotNull(this.loginController.processFormSubmission(
            new MockHttpServletRequest(), new MockHttpServletResponse(), c,
            new BindException(c, "credentials")).getModel().get(
            WebConstants.LOGIN_TOKEN));
    }
}
