package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.Credential;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdCredential implements Credential {

    private static final long serialVersionUID = -6535869729412406133L;

    private String ticketGrantingTicketId;

    private String username;

    /**
     * Instantiates a new OpenID credential.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @param username the username
     */
    public OpenIdCredential(final String ticketGrantingTicketId, final String username) {
        this.ticketGrantingTicketId = ticketGrantingTicketId;
        this.username = username;
    }

    public String getTicketGrantingTicketId() {
        return this.ticketGrantingTicketId;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public String toString() {
        return this.username;
    }

}
