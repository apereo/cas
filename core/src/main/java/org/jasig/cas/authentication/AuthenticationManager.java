/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Authenticate logon presenting a single Credential.
 * Typically an AuthenticationManagerImpl class.
 * 
 * <p>An AuthenticationManager must be attached to the same named
 * property in the CentralAuthenticationServiceImpl. Typically it is
 * defined in the userConfigContext.xml file and is then autowired by
 * type to the centralAuthenticationService bean defined in 
 * applicationContext.xml</p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface AuthenticationManager {

    /**
     * Verify Credentials, create a Principal, and return it wrapped by
     * an Authentication object that can be used to construct a TGT.
     * Any falure must throw AuthenticationException 
     * 
     * @param Opaque Credentials known to the manager or one of its plugins
     * @return Authentication object containing a Principal (may not be null)
     * @throws AuthenticationException on any failure
     */
    Authentication authenticate(final Credentials credentials)
        throws AuthenticationException;
}
