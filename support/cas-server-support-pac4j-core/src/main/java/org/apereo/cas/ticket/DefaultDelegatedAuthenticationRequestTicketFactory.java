package org.apereo.cas.ticket;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Map;

/**
 * This is {@link DefaultDelegatedAuthenticationRequestTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DefaultDelegatedAuthenticationRequestTicketFactory implements DelegatedAuthenticationRequestTicketFactory {
    private UniqueTicketIdGenerator ticketIdGenerator = new DefaultUniqueTicketIdGenerator();
    private final ExpirationPolicy expirationPolicy;
    /**
     * Create delegated authentication request ticket.
     *
     * @param service          the service
     * @param properties       the properties
     * @return the delegated authentication request ticket
     */
    @Override
    public DelegatedAuthenticationRequestTicket create(final Service service,
                                                       final Map<String, Object> properties) {
        final String id = ticketIdGenerator.getNewTicketId(DelegatedAuthenticationRequestTicket.PREFIX);
        return new DelegatedAuthenticationRequestTicket(id, expirationPolicy, service, properties);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
