/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;

/**
 * Default implementation of AuthenticationManager. Default implementation accepts a list of handlers. It will iterate through the list of handlers
 * and return the principal for the first one that can validate the request.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */

public class AuthenticationManagerImpl implements AuthenticationManager {

    protected final Log log = LogFactory.getLog(getClass());

    private List authenticationHandlers;

    private List credentialsToPrincipalResolvers;

    /**
     * @see org.jasig.cas.authentication.AuthenticationManager#authenticateUser(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public Principal authenticateUser(final AuthenticationRequest request) {
        for (Iterator iter = this.authenticationHandlers.iterator(); iter.hasNext();) {
            final AuthenticationHandler handler = (AuthenticationHandler)iter.next();

            if (handler.supports(request) && handler.authenticate(request)) {
                for (Iterator resolvers = this.credentialsToPrincipalResolvers.iterator(); resolvers.hasNext();) {
                    CredentialsToPrincipalResolver resolver = (CredentialsToPrincipalResolver)resolvers.next();

                    if (resolver.supports(request))
                        return resolver.resolvePrincipal(request);
                }
                return null;
            }

        }
        return null;
    }

    /**
     * @param authenticationHandlers The authenticationHandlers to set.
     */
    public void setAuthenticationHandlers(final List authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * @param credentialsToPrincipalResolvers The credentialsToPrincipalResolvers to set.
     */
    public void setCredentialsToPrincipalResolvers(List credentialsToPrincipalResolvers) {
        this.credentialsToPrincipalResolvers = credentialsToPrincipalResolvers;
    }
}
