package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

/**
 * Default OAuth refresh token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class DefaultRefreshTokenFactory implements RefreshTokenFactory {

    /** Default instance for the ticket id generator. */
    protected final UniqueTicketIdGenerator refreshTokenIdGenerator;

    /** ExpirationPolicy for refresh tokens. */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultRefreshTokenFactory(final ExpirationPolicy expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }

    public DefaultRefreshTokenFactory(final UniqueTicketIdGenerator refreshTokenIdGenerator, final ExpirationPolicy expirationPolicy) {
        this.refreshTokenIdGenerator = refreshTokenIdGenerator;
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public RefreshToken create(final Service service, final Authentication authentication) {
        final String codeId = this.refreshTokenIdGenerator.getNewTicketId(RefreshToken.PREFIX);
        return new RefreshTokenImpl(codeId, service, authentication, this.expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
