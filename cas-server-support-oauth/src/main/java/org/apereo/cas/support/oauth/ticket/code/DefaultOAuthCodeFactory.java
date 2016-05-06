package org.apereo.cas.support.oauth.ticket.code;

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
 * Default OAuth code factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultOAuthCodeFactory")
public class DefaultOAuthCodeFactory implements OAuthCodeFactory {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    
    @Autowired(required = false)
    @Qualifier("oAuthCodeIdGenerator")
    protected UniqueTicketIdGenerator oAuthCodeIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** ExpirationPolicy for OAuth code. */
    
    @Autowired
    @Qualifier("oAuthCodeExpirationPolicy")
    protected ExpirationPolicy expirationPolicy;

    @Override
    public OAuthCode create(final Service service, final Authentication authentication) {
        final String codeId = this.oAuthCodeIdGenerator.getNewTicketId(OAuthCode.PREFIX);
        return new OAuthCodeImpl(codeId, service, authentication, this.expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    /**
     * Get the OAuth code identifier generator.
     *
     * @return the OAuth code identifier generator.
     */
    public UniqueTicketIdGenerator getoAuthCodeIdGenerator() {
        return this.oAuthCodeIdGenerator;
    }

    /**
     * Set the OAuth code identifier generator.
     *
     * @param oAuthCodeIdGenerator the OAuth code identifier generator.
     */
    public void setoAuthCodeIdGenerator(final UniqueTicketIdGenerator oAuthCodeIdGenerator) {
        this.oAuthCodeIdGenerator = oAuthCodeIdGenerator;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return this.expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
