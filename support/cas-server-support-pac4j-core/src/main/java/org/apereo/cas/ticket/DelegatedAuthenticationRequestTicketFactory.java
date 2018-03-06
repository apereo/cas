package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationRequestTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface DelegatedAuthenticationRequestTicketFactory extends TicketFactory {
    /**
     * Create delegated authentication request ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     */
    DelegatedAuthenticationRequestTicket create(Service service, Map<String, Object> properties);

    /**
     * Create delegated authentication request ticket.
     *
     * @param service the service
     * @return the delegated authentication request ticket
     */
    default DelegatedAuthenticationRequestTicket create(final Service service) {
        return create(service, new LinkedHashMap<>());
    }
}
