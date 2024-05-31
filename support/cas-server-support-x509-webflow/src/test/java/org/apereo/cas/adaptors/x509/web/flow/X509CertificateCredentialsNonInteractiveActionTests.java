package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Tag("X509")
class X509CertificateCredentialsNonInteractiveActionTests extends BaseCertificateCredentialActionTests {

    @Test
    void verifyNoCredentialsResultsInError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }

    @Test
    void verifyBadCertificateError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestAttribute(X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_CERTIFICATE,
            new X509Certificate[]{new CasX509Certificate(false)});
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    @Test
    void verifyCredentialsResultsInSuccess() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestAttribute(X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_CERTIFICATE, new X509Certificate[]{VALID_CERTIFICATE});
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertTrue(context.getFlowScope().contains(X509Certificate.class.getName()));
    }

    @Test
    void verifyErrorInRequestResultsInError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestAttribute(X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_CERTIFICATE, new X509Certificate[]{VALID_CERTIFICATE});
        context.getRequestScope().put(X509CertificateCredentialsNonInteractiveAction.REQUEST_ATTRIBUTE_X509_ERROR, "true");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }

}
