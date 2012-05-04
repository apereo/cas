/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.authentication.principal;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public final class OpenIdCredentials implements Credentials {

    /**
     * Unique Id for Serialization
     */
    private static final long serialVersionUID = -6535869729412406133L;

    private final String ticketGrantingTicketId;
    
    private final String username;
    
    public OpenIdCredentials(final String ticketGrantingTicketId, final String username) {
        this.ticketGrantingTicketId = ticketGrantingTicketId;
        this.username = username;
    }
    
    public String getTicketGrantingTicketId() {
        return this.ticketGrantingTicketId;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String toString() {
        return "username: " + this.username;
    }
    
}
