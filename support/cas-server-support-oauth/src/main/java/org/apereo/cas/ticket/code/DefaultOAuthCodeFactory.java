package org.apereo.cas.ticket.code;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Collection;

/**
 * Default OAuth code factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultOAuthCodeFactory implements OAuthCodeFactory {

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator oAuthCodeIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultOAuthCodeFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }
    
    @Override
    public OAuthCode create(final Service service, final Authentication authentication,
                            final TicketGrantingTicket ticketGrantingTicket, final Collection<String> scopes) {
        final String codeId = this.oAuthCodeIdGenerator.getNewTicketId(OAuthCode.PREFIX);
        return new OAuthCodeImpl(codeId, service, authentication,
            this.expirationPolicy, ticketGrantingTicket, scopes);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
