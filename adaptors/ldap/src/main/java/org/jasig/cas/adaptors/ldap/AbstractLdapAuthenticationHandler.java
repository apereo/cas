/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.ldap.core.support.LdapDaoSupport;

/**
 * Abstract class that duplicates functionality of AbstractHandler
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractLdapAuthenticationHandler extends LdapDaoSupport
    implements AuthenticationHandler {

    public boolean authenticate(Credentials credentials)
        throws AuthenticationException {
        if (!this.supports(credentials))
            throw new UnsupportedCredentialsException();

        return this.authenticateInternal(credentials);
    }

    /**
     * @param credentials The credentials we want to check if the handler supports.
     * @return true if the handler supports authenticating this type of credentials. False otherwise.
     */
    protected abstract boolean supports(Credentials credentials);

    protected abstract boolean authenticateInternal(Credentials credentials)
        throws AuthenticationException;
}