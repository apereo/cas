/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractX509CertificateCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    /** Log instance. */
    protected Log log = LogFactory.getLog(this.getClass());

    public final Principal resolvePrincipal(final Credentials credentials) {
        final Principal principal = resolvePrincipalInternal(((X509CertificateCredentials) credentials).getCertificate());
        
        if (log.isInfoEnabled()) {
            log.info("Created Principal for: " + principal.getId());
        }
        
        return principal;
    }

    public final boolean supports(final Credentials credentials) {
        return credentials != null
            && X509CertificateCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    protected abstract Principal resolvePrincipalInternal(
        final X509Certificate certificate);
}
