/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Simple test implementation of a AuthenticationHandler that returns a true if
 * the username and password match.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {

    /** Log instance. */
    private final Log log = LogFactory.getLog(getClass());

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {
        final String username = credentials.getUsername();
        final String password = credentials.getPassword();

        if (StringUtils.hasText(username) && StringUtils.hasText(password)
            && username.equals(password)) {
            log
                .debug("User [" + username
                    + "] was successfully authenticated.");
            return true;
        }

        log.debug("User [" + username + "] failed authentication");

        return false;
    }

    public void afterPropertiesSet() throws Exception {
        log
            .warn(this.getClass().getName()
                + " is only to be used in a testing environment.  NEVER enable this in a production environment.");
    }
}
