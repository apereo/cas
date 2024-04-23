package org.apereo.cas.web.flow;

import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class X509CertificateCredentialsRequestHeaderAction extends X509CertificateCredentialsNonInteractiveAction {

    private final X509CertificateExtractor x509CertificateExtractor;

    public X509CertificateCredentialsRequestHeaderAction(final CasDelegatingWebflowEventResolver eventResolver,
                                                         final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                         final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                         final X509CertificateExtractor x509CertificateExtractor,
                                                         final CasConfigurationProperties casProperties) {
        super(eventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy, casProperties);
        this.x509CertificateExtractor = x509CertificateExtractor;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        if (context.getRequestScope().contains(REQUEST_ATTRIBUTE_X509_ERROR)) {
            LOGGER.debug("Not getting certificates from header because error found in the request.");
            return null;
        }

        val x509Credential = super.constructCredentialsFromRequest(context);
        if (x509Credential != null) {
            return x509Credential;
        }
        if (x509CertificateExtractor != null) {
            val certFromHeader = x509CertificateExtractor.extract(request);
            if (certFromHeader != null) {
                LOGGER.debug("Certificate found in HTTP request via [{}]", x509CertificateExtractor.getClass().getName());
                return new X509CertificateCredential(certFromHeader);
            }
            LOGGER.debug("Certificates not found in request header.");
        } else {
            LOGGER.trace("No certificate extractor was configured");
        }
        return null;
    }

    @Override
    protected void onError(final RequestContext requestContext) {
        WebUtils.putCasLoginFormViewable(requestContext,
            WebUtils.isCasLoginFormSetToViewable(requestContext) || casProperties.getAuthn().getX509().isMixedMode());
        requestContext.getRequestScope().put(REQUEST_ATTRIBUTE_X509_ERROR, "true");
    }
}
