package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link TransientSessionTicket} that allows CAS to use the ticket registry
 * as a distributed session store for short-lived non-specific objects. The intention
 * of this ticket is to encapsulate objects that would otherwise be tracked by the application
 * container's session. By using the ticket registry as a generic session store, all operations
 * that may require session awareness especially in a clustered environment can be freed from
 * that requirement.
 * <p>
 * Note that objects/values put into the session ticket are required to be serializable,
 * just as normal ticket properties would be, depending on the design of the underlying ticket registry.
 * <p>
 * Transient tickets generally have prominent use when CAS is acting as a proxy to another identity provider
 * where the results of current application session/request need to be stored across the cluster and remembered later.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TransientSessionTicket extends TicketGrantingTicketAwareTicket, ServiceAwareTicket, PropertiesAwareTicket {
    /**
     * Ticket prefix for the delegated authentication request.
     */
    String PREFIX = "TST";
}
