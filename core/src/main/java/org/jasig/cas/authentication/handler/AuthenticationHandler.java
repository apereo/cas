/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for the handlers that validate credentials for authentication.
 * Developers deploying CAS supply an AuthenticationHandler and plug it into the
 * AuthenticationManager. Implementations of this interface might be backed by
 * such things as LDAP servers, Kerberos realms, RDBMS tables, etc.
 * <p>A call to the authenticate method can assume that the corresponding supports
 * method was called and check first.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface AuthenticationHandler {

    /**
     * Method to determine if the credentials supplied can be authenticated.
     * 
     * @param credentials The credentials to authenticate
     * @return true if authenticated and false they are not
     * @throws AuthenticationException An AuthenticationException can contain
     * details about why a particular authentication request failed.
     * AuthenticationExceptions contain code/desc.
     */
    boolean authenticate(Credentials credentials)
        throws AuthenticationException;
    
    /**
     * Method to check if the handler knows how to handle these credentials.  It may
     * be a simple check of the Credentials class or something more complicated such as
     * scanning the information contained in the Credentials object.
     * 
     * @param credentials The credentials to check.
     * @return true if the handler supports the Credentials, false othewrise.
     */
    boolean supports(Credentials credentials);
}
