package org.apereo.cas.ticket;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apereo.cas.util.EncodingUtils;

/**
 * This is {@link DefaultSecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultSecurityTokenTicketFactory implements SecurityTokenTicketFactory {

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicy expirationPolicy;

    public DefaultSecurityTokenTicketFactory(final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    @Override
    public SecurityTokenTicket create(final TicketGrantingTicket ticket, final SecurityToken securityToken) {
        final String token = EncodingUtils.encodeBase64(SerializationUtils.serialize(securityToken));
        final String id = ticket.getId().replace(TicketGrantingTicket.PREFIX, SecurityTokenTicket.PREFIX);
        return new DefaultSecurityTokenTicket(id, ticket, this.expirationPolicy, token);
    }
}
