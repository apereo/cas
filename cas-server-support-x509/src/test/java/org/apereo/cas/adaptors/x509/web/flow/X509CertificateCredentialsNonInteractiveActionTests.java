package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.AbstractCentralAuthenticationService;
import org.apereo.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.*;


/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class X509CertificateCredentialsNonInteractiveActionTests extends AbstractX509CertificateTests {

    private X509CertificateCredentialsNonInteractiveAction action;

    @Before
    public void setUp() throws Exception {
        this.action = new X509CertificateCredentialsNonInteractiveAction();

        final X509CredentialsAuthenticationHandler handler = new X509CredentialsAuthenticationHandler();
        handler.setTrustedIssuerDnPattern("CN=\\w+,DC=jasig,DC=org");

        final AuthenticationManager authenticationManager = new PolicyBasedAuthenticationManager(
                Collections.<AuthenticationHandler, PrincipalResolver>singletonMap(
                        handler, new X509SerialNumberPrincipalResolver()));

        final AbstractCentralAuthenticationService centralAuthenticationService = (AbstractCentralAuthenticationService)
                getCentralAuthenticationService();

        this.action.setCentralAuthenticationService(centralAuthenticationService);
        this.action.getAuthenticationSystemSupport().getAuthenticationTransactionManager()
                .setAuthenticationManager(authenticationManager);

    }

    @Test
    public void verifyNoCredentialsResultsInError() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyCredentialsResultsInSuccess() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("javax.servlet.request.X509Certificate", new X509Certificate[] {VALID_CERTIFICATE});
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}
