/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for processing a request for authentication.
 * 
 * @author Scott Battaglia
 * @version $Id: AuthenticationManager.java,v 1.2 2005/02/27 05:49:26 sbattaglia
 * Exp $
 */
public interface AuthenticationManager {

    /**
     * Method to confirm credentials from an authentication request and map
     * those credentials to a principal.
     * 
     * @param credentials the authentication credentials
     * @return the Principal the credentials authenticate or it throws an
     * exception otherwise
     */
    public Authentication authenticateAndResolveCredentials(
        final Credentials credentials) throws AuthenticationException;
}