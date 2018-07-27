package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuthCodeImpl;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collection;

/**
 * An OAuth refresh token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(RefreshToken.PREFIX)
@NoArgsConstructor
public class RefreshTokenImpl extends OAuthCodeImpl implements RefreshToken {

    private static final long serialVersionUID = -3544459978950667758L;

    /**
     * Constructs a new refresh token with unique id for a service and authentication.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param authentication       the authentication.
     * @param expirationPolicy     the expiration policy.
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public RefreshTokenImpl(final String id, final Service service,
                            final Authentication authentication,
                            final ExpirationPolicy expirationPolicy,
                            final TicketGrantingTicket ticketGrantingTicket,
                            final Collection<String> scopes,
                            final String codeChallenge,
                            final String codeChallengeMethod) {
        super(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes, codeChallenge, codeChallengeMethod);
    }

    public RefreshTokenImpl(final String id, final Service service,
                            final Authentication authentication,
                            final ExpirationPolicy expirationPolicy,
                            final TicketGrantingTicket ticketGrantingTicket,
                            final Collection<String> scopes) {
        this(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes, null, null);
    }

    @Override
    public String getPrefix() {
        return RefreshToken.PREFIX;
    }
}
