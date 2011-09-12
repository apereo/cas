/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Validate Credentials support for AuthenticationManagerImpl.
 * <p>
 * Determines that Credentials are valid. Password-based credentials may be
 * tested against an external LDAP, Kerberos, JDBC source. Certificates may be
 * checked against a list of CA's and do the usual chain validation.
 * Implementations must be parameterized with their sources of information.
 * <p>
 * Callers to this class should first call supports to determine if the
 * AuthenticationHandler can authenticate the credentials provided.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface AuthenticationHandler {

    /**
     * Method to determine if the credentials supplied are valid.
     * 
     * @param credentials The credentials to validate.
     * @return true if valid, return false otherwise.
     * @throws AuthenticationException An AuthenticationException can contain
     * details about why a particular authentication request failed.
     */
    boolean authenticate(Credentials credentials)
        throws AuthenticationException;

    /**
     * Method to check if the handler knows how to handle the credentials
     * provided. It may be a simple check of the Credentials class or something
     * more complicated such as scanning the information contained in the
     * Credentials object.
     * 
     * @param credentials The credentials to check.
     * @return true if the handler supports the Credentials, false othewrise.
     */
    boolean supports(Credentials credentials);
}
