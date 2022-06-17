package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import java.util.Map;

/**
 * This is {@link AuthenticatedServicesAwareTicketGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface AuthenticatedServicesAwareTicketGrantingTicket extends TicketGrantingTicket {

    Map<String, Service> getAuthenticatedServices();
}
