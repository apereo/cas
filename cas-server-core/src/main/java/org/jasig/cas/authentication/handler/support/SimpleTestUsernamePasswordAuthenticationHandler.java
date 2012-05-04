/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.StringUtils;

/**
 * Simple test implementation of a AuthenticationHandler that returns true if
 * the username and password match. This class should never be enabled in a
 * production environment and is only designed to facilitate unit testing and
 * load testing.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    public SimpleTestUsernamePasswordAuthenticationHandler() {
        log
            .warn(this.getClass().getName()
                + " is only to be used in a testing environment.  NEVER enable this in a production environment.");
    }

    public boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) {
        final String username = credentials.getUsername();
        final String password = credentials.getPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password)
            && username.equals(getPasswordEncoder().encode(password))) {
            log
                .debug("User [" + username
                    + "] was successfully authenticated.");
            return true;
        }

        log.debug("User [" + username + "] failed authentication");

        return false;
    }
}
