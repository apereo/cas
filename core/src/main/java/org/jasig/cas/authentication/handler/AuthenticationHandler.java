/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Validate Credentials support for AuthenticationManagerImpl.
 * 
 * <p>Determines that Credentials are valid. Password-based credentials
 * may be tested against an external LDAP, Kerberos, JDBC source. 
 * Certificates may be checked against a list of CA's and do the
 * usual chain validation. Implementations must be parameterized with
 * their sources of information.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface AuthenticationHandler {

    /**
     * Method to determine if the credentials supplied are valid.
     * 
     * @param credentials The credentials to validate.
     * @return true if valid. Do not return false, throw the exception.
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
     * <p>In AuthenticationManagerImpl, the first handler to claim that it
     * supports a particular Credentials object is the only Handler that 
     * gets to validate them. So if you want to allow subsequent handlers
     * to get a chance to look at these credentials and cannot validate them
     * yourself, return false from this method.
     * 
     * @param credentials The credentials to check.
     * @return true if the handler supports the Credentials, false othewrise.
     */
    boolean supports(Credentials credentials);
}
