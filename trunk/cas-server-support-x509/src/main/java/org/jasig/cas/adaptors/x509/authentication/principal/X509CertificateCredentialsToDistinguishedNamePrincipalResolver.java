/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class X509CertificateCredentialsToDistinguishedNamePrincipalResolver
    extends AbstractX509CertificateCredentialsToPrincipalResolver {

    protected String resolvePrincipalInternal(
        final X509Certificate certificate) {
        return certificate.getSubjectDN().getName();
    }
}
