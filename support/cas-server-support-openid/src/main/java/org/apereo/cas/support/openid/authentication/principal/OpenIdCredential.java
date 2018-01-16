package org.apereo.cas.support.openid.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import lombok.ToString;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@ToString
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
}
