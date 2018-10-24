package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link X509CertificateCredentialsRequestHeaderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.x509.extractCert=true")
public class X509CertificateCredentialsRequestHeaderActionTests extends BaseCertificateCredentialActionTests {

    @Test
    public void verifyCredentialsResultsInAuthnFailure() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("ssl_client_cert", VALID_CERTIFICATE.getContent());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.getObject().execute(context).getId());
    }
}
