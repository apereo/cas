package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.security.cert.X509Certificate;

import static org.junit.Assert.*;


/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@TestPropertySource(locations = {"classpath:/x509.properties"})
@Import(value = {X509AuthenticationWebflowConfiguration.class, X509AuthenticationConfiguration.class})
public class X509CertificateCredentialsNonInteractiveActionTests extends AbstractX509CertificateTests {

    @Autowired
    @Qualifier("x509Check")
    private Action action;

    @Test
    public void verifyNoCredentialsResultsInError() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

    @Test
    public void verifyCredentialsResultsInSuccess() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setAttribute(X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_CERTIFICATE, new X509Certificate[]{VALID_CERTIFICATE});
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }
}
