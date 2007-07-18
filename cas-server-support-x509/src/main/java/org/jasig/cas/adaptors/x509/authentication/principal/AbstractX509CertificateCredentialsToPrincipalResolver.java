/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractX509CertificateCredentialsToPrincipalResolver
    extends AbstractPersonDirectoryCredentialsToPrincipalResolver {

    protected String extractPrincipalId(final Credentials credentials) {
        return resolvePrincipalInternal(((X509CertificateCredentials) credentials).getCertificate());
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && X509CertificateCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    protected abstract String resolvePrincipalInternal(
        final X509Certificate certificate);
}
