package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for a Service Ticket. A service ticket is used to grant access to a
 * specific service for a principal. A Service Ticket is generally a one-time
 * use ticket.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ServiceTicket extends TicketGrantingTicketAwareTicket, ServiceAwareTicket {

    /**
     * Prefix generally applied to unique ids generated
     * by UniqueTicketIdGenerator.
     */
    String PREFIX = "ST";
}
