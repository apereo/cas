package org.apereo.cas.web.flow;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.extractcert.ExtractX509Certificate;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;

/**
 * Concrete implementation of AbstractNonInteractiveCredentialsAction that
 * obtains the X509 Certificates from the HttpServletRequest and places them in
 * the X509CertificateCredential.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class X509CertificateCredentialsNonInteractiveAction extends AbstractNonInteractiveCredentialsAction {
    
    private static final String CERTIFICATE_REQUEST_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    private boolean extractCertificateFromRequest;

    @Autowired(required = false)
    private ExtractX509Certificate x509ExtractSSLCertificate;
    
    public X509CertificateCredentialsNonInteractiveAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                          final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                          final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    public X509CertificateCredentialsNonInteractiveAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final boolean extractCert) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.extractCertificateFromRequest = extractCert;
    }

    
    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final X509Certificate[] certificates = (X509Certificate[]) context.getExternalContext().getRequestMap().get(CERTIFICATE_REQUEST_ATTRIBUTE);

        if (certificates == null || certificates.length == 0) {
            if (extractCertificateFromRequest) {
                final X509Certificate[] certsfromHeader = x509ExtractSSLCertificate.extract((HttpServletRequest) context.getExternalContext().getNativeRequest());
                if (certsfromHeader != null) {
                    LOGGER.debug("Certificate found in HTTP request via {}", x509ExtractSSLCertificate.getClass().getName());
                    return new X509CertificateCredential(certsfromHeader);
                }
            } 
            LOGGER.debug("Certificates not found in request.");
            return null;
        }
        LOGGER.debug("Certificate found in request.");
        return new X509CertificateCredential(certificates);
    }
}
