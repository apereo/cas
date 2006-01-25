/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.webflow.RequestContext;

/**
 * Concrete implementation of AbstractNonInteractiveCredentialsAction that
 * obtains the X509 Certificates from the HttpServletRequest and places them in
 * the X509CertificateCredentials.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class X509CertificateCredentialsNonInteractiveAction extends
    AbstractNonInteractiveCredentialsAction {

    private static final String CERTIFICATE_REQUEST_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    protected Credentials constructCredentialsFromRequest(
        final RequestContext context) {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final X509Certificate[] certificates = (X509Certificate[]) request
            .getAttribute(CERTIFICATE_REQUEST_ATTRIBUTE);

        if (certificates == null || certificates.length == 0) {
            return null;
        }

        return new X509CertificateCredentials(certificates);
    }
}
