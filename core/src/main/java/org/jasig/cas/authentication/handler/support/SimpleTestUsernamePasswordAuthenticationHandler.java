/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.StringUtils;

/**
 * Simple test implementation of a AuthenticationHandler that returns a true if
 * the username and password match.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class SimpleTestUsernamePasswordAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {
        final String username = credentials.getUserName();
        final String password = credentials.getPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password)
            && username.equals(password)) {
            log.debug("User [" + username //$NON-NLS-1$
                + "] was successfully authenticated."); //$NON-NLS-1$
            return true;
        }

        log.debug("User [" + username + "] failed authentication"); //$NON-NLS-2$

        return false;
    }

    public void afterPropertiesSet() throws Exception {
        log
            .warn(this.getClass().getName()
                + " is only to be used in a testing environment.  NEVER enable this in a production environment.");
    }
}
