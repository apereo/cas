/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.StringUtils;

/**
 * Simple test implementation of a AuthenticationHandler that returns a true if the username and password match.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimpleTestUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticateInternal(final Credentials credentials) throws AuthenticationException {
        final UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
        final String username = usernamePasswordCredentials.getUserName();
        final String password = usernamePasswordCredentials.getPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password) && username.equals(password)) {
            log.debug("User [" + username + "] was successfully authenticated.");
            return true;
        }

        log.debug("User [" + username + "] failed authentication");
        return false;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        log.warn(this.getClass().getName() + " is only to be used in a testing environment.  NEVER enable this in a production environment.");
    }
}
