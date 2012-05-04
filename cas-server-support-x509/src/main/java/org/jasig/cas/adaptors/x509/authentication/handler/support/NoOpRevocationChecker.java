/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;


/**
 * NO-OP implementation certificate revocation checker.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.6
 *
 */
public final class NoOpRevocationChecker implements RevocationChecker {

    /**
     * NO-OP check implementation.
     *
     * @param certificate Certificate to check.
     *
     * @throws GeneralSecurityException Never thrown.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationChecker#check(java.security.cert.X509Certificate)
     */
    public void check(final X509Certificate certificate) throws GeneralSecurityException {
        // NO-OP
    }

}
