/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for the handlers that validate credentials for authentication. Developers deploying CAS supply an AuthenticationHandler and plug it into the
 * AuthenticationManager. Implementations of this interface might be backed by such things as LDAP servers, Kerberos realms, RDBMS tables, etc.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface AuthenticationHandler {

    /**
     * Method to determine if the credentials supplied can be authenticated.
     * @param credentials The credentials to authenticate
     * @return true if authenticated and false otherwise
     * @throws AuthenticationException An AuthenticationException can contain details about why
     * a particular authentication request failed.  AuthenticationExceptions contain code/desc.
     */
    boolean authenticate(final Credentials credentials) throws AuthenticationException;

    /**
     * @param request The credentials we want to check if the handler supports.
     * @return true if the handler supports authenticating this type of request. False otherwise.
     */
    boolean supports(final Credentials credentials);
}
