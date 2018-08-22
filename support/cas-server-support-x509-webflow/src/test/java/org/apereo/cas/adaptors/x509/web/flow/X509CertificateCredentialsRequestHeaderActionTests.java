package org.apereo.cas.adaptors.x509.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.web.extractcert.X509CertificateExtractorConfiguration;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;
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

import static org.junit.Assert.*;

/**
 * This is {@link X509CertificateCredentialsRequestHeaderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(locations = {"classpath:/x509.properties"},
    properties = "cas.authn.x509.extractCert=true")
@Import(value = {X509AuthenticationWebflowConfiguration.class, X509AuthenticationConfiguration.class, X509CertificateExtractorConfiguration.class})
@Slf4j
public class X509CertificateCredentialsRequestHeaderActionTests extends AbstractX509CertificateTests {

    @Autowired
    @Qualifier("x509Check")
    private Action action;

    @Test
    public void verifyCredentialsResultsInAuthnFailure() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("ssl_client_cert", VALID_CERTIFICATE.getContent());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }
}
