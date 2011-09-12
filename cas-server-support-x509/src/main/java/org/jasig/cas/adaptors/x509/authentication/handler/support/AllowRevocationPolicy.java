/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements an unqualified allow policy.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.6
 *
 */
public final class AllowRevocationPolicy implements RevocationPolicy<Void> {
    /** Logger instance */
    private final Log log = LogFactory.getLog(getClass());


    /**
     * Policy application does nothing to implement unqualfied allow.
     *
     * @param data SHOULD be null; ignored in all cases.
     * 
     * @throws GeneralSecurityException Never thrown.
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    public void apply(final Void data) throws GeneralSecurityException {
        log.info("Continuing since AllowRevocationPolicy is in effect.");
    }
}
