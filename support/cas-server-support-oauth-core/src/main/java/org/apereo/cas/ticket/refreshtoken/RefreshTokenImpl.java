package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.ExpirationPolicy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An OAuth refresh token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(RefreshToken.PREFIX)
public class RefreshTokenImpl extends OAuthCodeImpl implements RefreshToken {

    private static final long serialVersionUID = -3544459978950667758L;

    /**
     * Instantiates a new OAuth refresh token.
     */
    public RefreshTokenImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new refresh token with unique id for a service and authentication.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param authentication       the authentication.
     * @param expirationPolicy     the expiration policy.
     * @param ticketGrantingTicket the ticket granting ticket
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public RefreshTokenImpl(final String id, final Service service, final Authentication authentication,
                            final ExpirationPolicy expirationPolicy, final TicketGrantingTicket ticketGrantingTicket) {
        super(id, service, authentication, expirationPolicy, ticketGrantingTicket);
    }

    @Override
    public String getPrefix() {
        return RefreshToken.PREFIX;
    }
}
