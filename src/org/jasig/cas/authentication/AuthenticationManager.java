/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Interface for processing a request for authentication.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface AuthenticationManager {

    /**
     * Method to confirm credentials from an authentication request and map those credentials to a principal.
     * 
     * @param request the authentication credentials
     * @return the Principal the credentials authenticate.
     */
    public Authentication authenticateAndResolveCredentials(final Credentials credentials) throws AuthenticationException;
}
