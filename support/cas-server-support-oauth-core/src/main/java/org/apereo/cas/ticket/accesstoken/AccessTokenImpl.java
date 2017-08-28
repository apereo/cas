package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuthCodeImpl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Collection;
import java.util.HashSet;

/**
 * An OAuth access token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(AccessToken.PREFIX)
public class AccessTokenImpl extends OAuthCodeImpl implements AccessToken {

    private static final long serialVersionUID = 2339545346159721563L;

    @Lob
    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>();
    
    /**
     * Instantiates a new OAuth access token.
     */
    public AccessTokenImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new access token with unique id for a service and authentication.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param authentication       the authentication.
     * @param expirationPolicy     the expiration policy.
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public AccessTokenImpl(final String id, final Service service, final Authentication authentication,
                           final ExpirationPolicy expirationPolicy, 
                           final TicketGrantingTicket ticketGrantingTicket,
                           final Collection<String> scopes) {
        super(id, service, authentication, expirationPolicy, ticketGrantingTicket);
        this.scopes.addAll(scopes);
    }

    @Override
    public String getPrefix() {
        return AccessToken.PREFIX;
    }

    @Override
    public Collection<String> getScopes() {
        return this.scopes;
    }
}
