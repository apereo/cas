package org.apereo.cas.support.oauth.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Default OAuth refresh token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultRefreshTokenFactory")
public class DefaultRefreshTokenFactory implements RefreshTokenFactory {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    
    @Autowired(required = false)
    @Qualifier("refreshTokenIdGenerator")
    protected UniqueTicketIdGenerator refreshTokenIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** ExpirationPolicy for refresh tokens. */
    
    @Autowired
    @Qualifier("refreshTokenExpirationPolicy")
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
