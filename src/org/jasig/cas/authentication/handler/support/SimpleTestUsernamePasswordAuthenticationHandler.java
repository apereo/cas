/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
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
    public boolean authenticate(final AuthenticationRequest request) {
        final UsernamePasswordAuthenticationRequest authRequest = (UsernamePasswordAuthenticationRequest)request;
        final String username = authRequest.getUserName();
        final String password = authRequest.getPassword();

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
    }
}
