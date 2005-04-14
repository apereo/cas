/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Event for letting listeners know about authentication requests
 * and whether they were successful or not.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class AuthenticationEvent extends AbstractEvent {

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 3257844398434889778L;
    
    /** Boolean of whether this event represents a successful authentication or not. */
    private boolean successfulAuthentication;

    public AuthenticationEvent(final Credentials credentials, final boolean successfulAuthentication) {
        super(credentials);
        
        this.successfulAuthentication = successfulAuthentication;
    }

    /**
     * Method to return the Credentials for the Authentication.
     * @return the Credentials.
     */
    public final Credentials getCredentials() {
        return (Credentials) getSource();
    }
    
    /**
     * Method to determine if the authentication this event represents was successful or not.
     * @return true if successful, false otherwise.
     */
    public final boolean isSuccessfulAuthentication() {
        return this.successfulAuthentication;
    }
}
