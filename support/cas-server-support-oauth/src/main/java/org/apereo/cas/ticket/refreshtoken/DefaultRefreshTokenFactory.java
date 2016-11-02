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
    protected UniqueTicketIdGenerator refreshTokenIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** ExpirationPolicy for refresh tokens. */
    protected ExpirationPolicy expirationPolicy;

    @Override
    public RefreshToken create(final Service service, final Authentication authentication) {
        final String codeId = this.refreshTokenIdGenerator.getNewTicketId(RefreshToken.PREFIX);
        return new RefreshTokenImpl(codeId, service, authentication, this.expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public UniqueTicketIdGenerator getRefreshTokenIdGenerator() {
        return this.refreshTokenIdGenerator;
    }

    public void setRefreshTokenIdGenerator(final UniqueTicketIdGenerator refreshTokenIdGenerator) {
        this.refreshTokenIdGenerator = refreshTokenIdGenerator;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return this.expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
