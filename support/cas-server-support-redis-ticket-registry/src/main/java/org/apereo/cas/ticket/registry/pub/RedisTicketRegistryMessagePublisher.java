package org.apereo.cas.ticket.registry.pub;

import module java.base;
import org.apereo.cas.ticket.Ticket;

/**
 * This is {@link RedisTicketRegistryMessagePublisher}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RedisTicketRegistryMessagePublisher {

    /**
     * Send notification to delete all.
     */
    void deleteAll();

    /**
     * Send notification to delete ticket id.
     *
     * @param ticket the ticket
     */
    void delete(Ticket ticket);

    /**
     * Send notification to add.
     *
     * @param id the id
     */
    void add(Ticket id);

    /**
     * Send notification to update ticket id.
     *
     * @param id the id
     */
    void update(Ticket id);
}
