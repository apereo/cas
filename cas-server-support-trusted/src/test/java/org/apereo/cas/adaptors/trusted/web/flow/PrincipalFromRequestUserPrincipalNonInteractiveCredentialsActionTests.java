package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationService;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Collections;

/**
 * @author Scott Battaglia
 * @since 3.0.5
 */
public class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {

    private PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction action;


    @Before
    public void setUp() throws Exception {
        this.action = new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction();
        this.action.setPrincipalFactory(new DefaultPrincipalFactory());

        final AuthenticationManager authenticationManager = new PolicyBasedAuthenticationManager(
                Collections.<AuthenticationHandler, PrincipalResolver>singletonMap(
                        new PrincipalBearingCredentialsAuthenticationHandler(),
                        new PrincipalBearingPrincipalResolver()));

        final AbstractCentralAuthenticationService centralAuthenticationService = (AbstractCentralAuthenticationService)
                getCentralAuthenticationService();
        this.action.setCentralAuthenticationService(centralAuthenticationService);
        this.action.getAuthenticationSystemSupport().getAuthenticationTransactionManager()
                .setAuthenticationManager(authenticationManager);
    }

    @Test
    public void verifyRemoteUserExists() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(() -> "test");

        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));

        Assert.assertEquals("success", this.action.execute(context).getId());
    }

    @Test
    public void verifyRemoteUserDoesntExists() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));

        Assert.assertEquals("error", this.action.execute(context).getId());
    }

}
