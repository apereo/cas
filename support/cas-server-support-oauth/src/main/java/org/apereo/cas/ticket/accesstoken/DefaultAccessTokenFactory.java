package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class DefaultAccessTokenFactory implements AccessTokenFactory {

    /** Default instance for the ticket id generator. */
    protected final UniqueTicketIdGenerator accessTokenIdGenerator;

    /** ExpirationPolicy for refresh tokens. */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultAccessTokenFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }

    public DefaultAccessTokenFactory(final UniqueTicketIdGenerator refreshTokenIdGenerator, final ExpirationPolicy expirationPolicy) {
        this.accessTokenIdGenerator = refreshTokenIdGenerator;
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public AccessToken create(final Service service, final Authentication authentication) {
        final String codeId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        return new AccessTokenImpl(codeId, service, authentication, this.expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
