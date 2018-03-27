package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link TransientSessionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface TransientSessionTicketFactory extends TicketFactory {
    /**
     * Create delegated authentication request ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     */
    TransientSessionTicket create(Service service, Map<String, Serializable> properties);

    /**
     * Create delegated authentication request ticket.
     *
     * @param service the service
     * @return the delegated authentication request ticket
     */
    default TransientSessionTicket create(final Service service) {
        return create(service, new LinkedHashMap<>());
    }
}
