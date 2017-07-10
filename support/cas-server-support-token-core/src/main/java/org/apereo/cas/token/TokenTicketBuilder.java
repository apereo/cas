package org.apereo.cas.token;

import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link TokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface TokenTicketBuilder {
    /**
     * Build token.
     *
     * @param ticketId the ticket id
     * @param service  the service
     * @return the token identifier
     */
    String build(String ticketId, Service service);
}
