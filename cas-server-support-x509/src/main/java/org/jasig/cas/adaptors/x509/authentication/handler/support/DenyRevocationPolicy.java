/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;


/**
 * Implements a deny policy by throwing an exception.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.6
 *
 */
public final class DenyRevocationPolicy implements RevocationPolicy<Void> {

    /**
     * Policy application throws GeneralSecurityException to stop execution of
     * whatever process invoked application of this policy.
     *
     * @param nothing SHOULD be null; ignored in all cases.
     * 
     * @throws GeneralSecurityException Thrown in all cases.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    public void apply(final Void nothing) throws GeneralSecurityException {
        throw new GeneralSecurityException("Aborting since DenyRevocationPolicy is in effect.");
    }

}
