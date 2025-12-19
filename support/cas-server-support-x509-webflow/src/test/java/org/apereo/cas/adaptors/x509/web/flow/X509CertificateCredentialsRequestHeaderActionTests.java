package org.apereo.cas.adaptors.x509.web.flow;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_ERROR;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509CertificateCredentialsRequestHeaderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.x509.extract-cert=true")
@Tag("X509")
class X509CertificateCredentialsRequestHeaderActionTests extends BaseCertificateCredentialActionTests {

    @Test
    void verifyCredentialsResultsInAuthnFailure() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("ssl_client_cert", VALID_CERTIFICATE.getContent());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
            action.execute(context).getId());
    }

    @Test
    void verifyErrorInRequestResultsInError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("ssl_client_cert", VALID_CERTIFICATE.getContent());
        context.getRequestScope().put(REQUEST_ATTRIBUTE_X509_ERROR, "true");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }

}
