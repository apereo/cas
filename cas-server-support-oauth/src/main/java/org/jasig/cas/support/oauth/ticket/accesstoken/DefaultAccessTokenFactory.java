package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.AccessToken;
import org.jasig.cas.ticket.AccessTokenImpl;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Default OAuth access token factory.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Component("defaultAccessTokenFactory")
public class DefaultAccessTokenFactory implements AccessTokenFactory {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    @NotNull
    @Autowired(required = false)
    @Qualifier("accessTokenIdGenerator")
    protected UniqueTicketIdGenerator accessTokenIdGenerator = new DefaultUniqueTicketIdGenerator();


    /** ExpirationPolicy for access tokens. */
    @Autowired(required = false)
    @Qualifier("oAuthAcccessTokenExpirationPolicy")
    protected ExpirationPolicy expirationPolicy;

    @Override
    public AccessToken create(final Service service, final Authentication authentication) {
        CommonHelper.assertNotNull("expirationPolicy", expirationPolicy);

        final String codeId = accessTokenIdGenerator.getNewTicketId(AccessToken.PREFIX);
        return new AccessTokenImpl(codeId, service, authentication, expirationPolicy);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public UniqueTicketIdGenerator getAccessTokenIdGenerator() {
        return accessTokenIdGenerator;
    }

    public void setAccessTokenIdGenerator(final UniqueTicketIdGenerator accessTokenIdGenerator) {
        this.accessTokenIdGenerator = accessTokenIdGenerator;
    }

    public ExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    public void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }
}
