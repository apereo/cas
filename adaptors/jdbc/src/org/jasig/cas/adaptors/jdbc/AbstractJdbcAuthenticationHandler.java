/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Abstract class to duplicate AbstractHandler
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractJdbcAuthenticationHandler extends JdbcDaoSupport implements AuthenticationHandler {

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.principal.Credentials)
     */
    public boolean authenticate(Credentials credentials) throws AuthenticationException {
        if (!this.supports(credentials))
            throw new UnsupportedCredentialsException();

        return this.authenticateInternal(credentials);
    }

    /**
     * @param credentials The credentials we want to check if the handler supports.
     * @return true if the handler supports authenticating this type of credentials. False otherwise.
     */
    protected abstract boolean supports(Credentials credentials);

    protected abstract boolean authenticateInternal(Credentials credentials) throws AuthenticationException;
}
