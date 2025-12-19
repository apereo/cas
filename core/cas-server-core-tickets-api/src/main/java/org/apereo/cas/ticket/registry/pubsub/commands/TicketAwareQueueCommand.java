package org.apereo.cas.ticket.registry.pubsub.commands;

import module java.base;
import org.apereo.cas.util.PublisherIdentifier;

/**
 * This is {@link TicketAwareQueueCommand}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface TicketAwareQueueCommand {
    /**
     * Gets ticket.
     *
     * @return the ticket
     */
    String getTicketId();

    /**
     * Gets publisher identifier.
     *
     * @return the publisher identifier
     */
    PublisherIdentifier getPublisherIdentifier();
}
