/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Abstract authentication handler that allows deployers to utilize the bundled
 * AuthenticationHandlers while providing a mechanism to perform tasks before
 * and after authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractPreAndPostProcessingAuthenticationHandler
    implements NamedAuthenticationHandler {
    
    /** Instance of logging for subclasses. */
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** The name of the authentication handler. */
    @NotNull
    private String name = getClass().getName();

    /**
     * Method to execute before authentication occurs.
     * 
     * @param credentials the Credentials supplied
     * @return true if authentication should continue, false otherwise.
     */
    protected boolean preAuthenticate(final Credentials credentials) {
        return true;
    }

    /**
     * Method to execute after authentication occurs.
     * 
     * @param credentials the supplied credentials
     * @param authenticated the result of the authentication attempt.
     * @return true if the handler should return true, false otherwise.
     */
    protected boolean postAuthenticate(final Credentials credentials,
        final boolean authenticated) {
        return authenticated;
    }
    
    public final void setName(final String name) {
        this.name = name;
    }
    
    public final String getName() {
        return this.name;
    }

    public final boolean authenticate(final Credentials credentials)
        throws AuthenticationException {

        if (!preAuthenticate(credentials)) {
            return false;
        }

        final boolean authenticated = doAuthentication(credentials);

        return postAuthenticate(credentials, authenticated);
    }

    protected abstract boolean doAuthentication(final Credentials credentials)
        throws AuthenticationException;
}
