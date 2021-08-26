package org.apereo.cas.web.flow;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.security.cert.X509Certificate;
import java.util.Arrays;

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
    /**
     * Attribute to indicate the x509 certificate.
     */
    public static final String REQUEST_ATTRIBUTE_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";
    
    private static final String REQUEST_ATTRIBUTE_X509_ERROR = "X509CertificateAuthenticationError";

    /**
     * CAS configuration settings.
     */
    protected final CasConfigurationProperties casProperties;

    public X509CertificateCredentialsNonInteractiveAction(final CasDelegatingWebflowEventResolver webflowEventResolver,
                                                          final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                          final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                          final CasConfigurationProperties casProperties) {
        super(webflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.casProperties = casProperties;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val certificates = context.getRequestScope().contains(REQUEST_ATTRIBUTE_X509_ERROR)
            ? null
            : (X509Certificate[]) request.getAttribute(REQUEST_ATTRIBUTE_X509_CERTIFICATE);

        if (certificates == null || certificates.length == 0) {
            LOGGER.debug("Certificates not found in request attribute: [{}]", REQUEST_ATTRIBUTE_X509_CERTIFICATE);
            return null;
        }
        LOGGER.debug("[{}] Certificate(s) found in request: [{}]", certificates.length, Arrays.toString(certificates));
        return new X509CertificateCredential(certificates);
    }

    @Override
    protected void onError(final RequestContext requestContext) {
        WebUtils.putCasLoginFormViewable(requestContext, casProperties.getAuthn().getX509().isMixedMode());
        requestContext.getRequestScope().put(REQUEST_ATTRIBUTE_X509_ERROR, "true");
    }
}
