package org.jasig.cas.support.oauth.ticket.code;

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
 * Default OAuth code factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultOAuthCodeFactory")
public class DefaultOAuthCodeFactory implements OAuthCodeFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
        final String codeId = oAuthCodeIdGenerator.getNewTicketId(OAuthCode.PREFIX);
        return new OAuthCodeImpl(codeId, service, authentication, expirationPolicy);
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
        return oAuthCodeIdGenerator;
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
        return expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
