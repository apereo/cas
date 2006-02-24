/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalBearingCredentials;


/**
 * AuthenticationHandler which authenticates Principal-bearing credentials.
 * Authentication must have occured at the time the Principal-bearing credentials
 * were created, so we perform no further authentication.  If the PrincipalBearingCredentials
 * includes a non-null Principal, the Credentials authenticate successfully, otherwise
 * they do not.
 * @since 3.0.5
 * @version $revision:$ $date:$
 */
public final class PrincipalBearingCredentialsAuthenticationHandler 
    implements AuthenticationHandler {

    public boolean authenticate(Credentials credentials) {
        // while the interface allows throwing AuthenticationException, don't
        // declare it here, because this implementation doesn't throw it.
        
        PrincipalBearingCredentials principalBearingCredentials = (PrincipalBearingCredentials) credentials;
        
        Principal principal = principalBearingCredentials.getPrincipal();
        
        if (principal == null) {
            return false;
        }
        
        return true;
    }

    public boolean supports(Credentials credentials) {
        return credentials instanceof PrincipalBearingCredentials;
    }

}