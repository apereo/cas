/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * AuthenticationHandler which authenticates Principal-bearing credentials.
 * Authentication must have occured at the time the Principal-bearing
 * credentials were created, so we perform no further authentication. Thus
 * merely by being presented a PrincipalBearingCredentials, this handler returns
 * true.
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandler implements
    AuthenticationHandler {

    private Log log = LogFactory.getLog(this.getClass());

    public boolean authenticate(final Credentials credentials) {
        if (log.isDebugEnabled()) {
            log.debug("Trusting credentials for: " + credentials);
        }
        return true;
    }

    public boolean supports(final Credentials credentials) {
        return credentials.getClass().equals(PrincipalBearingCredentials.class);
    }
}