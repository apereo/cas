/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for the handlers that validate requests for authentication. Developers deploying CAS supply an AuthenticationHandler and plug it into the
 * AuthenticationManager. Implementations of this interface might be backed by such things as LDAP servers, Kerberos realms, RDBMS tables, etc.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface AuthenticationHandler {

    /**
     * Method to authenticate a request.
     * 
     * @param request the request to authenticate
     * @return true if the request is valid, false otherwise.
     */
    boolean authenticate(final Credentials credentials);

    /**
     * @param request The request we want to check if the handler supports.
     * @return true if the handler supports authenticating this type of request. False otherwise.
     */
    boolean supports(final Credentials credentials);
}
