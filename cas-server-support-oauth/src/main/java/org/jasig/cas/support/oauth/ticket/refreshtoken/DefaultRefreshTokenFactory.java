package org.jasig.cas.support.oauth.ticket.refreshtoken;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
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

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
        final String codeId = refreshTokenIdGenerator.getNewTicketId(RefreshToken.PREFIX);
        return new RefreshTokenImpl(codeId, service, authentication, expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public UniqueTicketIdGenerator getRefreshTokenIdGenerator() {
        return refreshTokenIdGenerator;
    }

    public void setRefreshTokenIdGenerator(final UniqueTicketIdGenerator refreshTokenIdGenerator) {
        this.refreshTokenIdGenerator = refreshTokenIdGenerator;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
