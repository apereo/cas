package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

/**
 * Default OAuth code factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class DefaultOAuthCodeFactory implements OAuthCodeFactory {

    /** Default instance for the ticket id generator. */
    protected UniqueTicketIdGenerator oAuthCodeIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** ExpirationPolicy for OAuth code. */
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
