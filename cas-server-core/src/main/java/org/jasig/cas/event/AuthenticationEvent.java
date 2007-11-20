/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

/**
 * Event for letting listeners know about authentication requests and whether
 * they were successful or not. Provides handlers with access to the original
 * Credentials object as well as the return value from the handler.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationEvent extends AbstractEvent {

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 3257844398434889778L;

    /**
     * Boolean of whether this event represents a successful authentication or
     * not.
     */
    private boolean successfulAuthentication;

    /** The AuthenticationHandler class used to generate this event. */
    private Class<?> authenticationHandlerClass;
    
    private String principal;

    /**
     * Constructs the AuthenticationEvent using the credentials as the source
     * object.
     * 
     * @param credentials the Credentials from the AuthenticationRequest.
     * @param successfulAuthentication boolean of whether the authentication was
     * successful or not.
     */
    public AuthenticationEvent(final String principal,
        final boolean successfulAuthentication,
        final Class<?> authenticationHandlerClass) {
        super(principal);

        this.successfulAuthentication = successfulAuthentication;
        this.authenticationHandlerClass = authenticationHandlerClass;
        this.principal = principal;
    }

    /**
     * Method to determine if the authentication this event represents was
     * successful or not.
     * 
     * @return true if successful, false otherwise.
     */
    public final boolean isSuccessfulAuthentication() {
        return this.successfulAuthentication;
    }

    /**
     * Method to return the AuthenticatonHandler class that generated the event.
     * 
     * @return the Class of the AuthenticationHandler.
     */
    public final Class<?> getAuthenticationHandlerClass() {
        return this.authenticationHandlerClass;
    }
    
    public String getPrincipal() {
        return this.principal;
    }
}
