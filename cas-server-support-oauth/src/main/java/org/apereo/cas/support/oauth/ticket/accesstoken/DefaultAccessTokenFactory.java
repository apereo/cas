package org.apereo.cas.support.oauth.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultAccessTokenFactory")
public class DefaultAccessTokenFactory implements AccessTokenFactory {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    
    @Autowired(required = false)
    @Qualifier("accessTokenIdGenerator")
    protected UniqueTicketIdGenerator accessTokenIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** ExpirationPolicy for access tokens. */
    
    @Autowired
    @Qualifier("accessTokenExpirationPolicy")
    protected ExpirationPolicy expirationPolicy;

    @Override
    public AccessToken create(final Service service, final Authentication authentication) {
        final String codeId = this.accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        return new AccessTokenImpl(codeId, service, authentication, this.expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public UniqueTicketIdGenerator getAccessTokenIdGenerator() {
        return this.accessTokenIdGenerator;
    }

    public void setAccessTokenIdGenerator(final UniqueTicketIdGenerator accessTokenIdGenerator) {
        this.accessTokenIdGenerator = accessTokenIdGenerator;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return this.expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
