package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the ticket registry that is backed by a map.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Getter
public class DefaultTicketRegistry extends AbstractMapBasedTicketRegistry {

    private final Map<String, Ticket> mapInstance;

    public DefaultTicketRegistry(final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog) {
        this(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog);
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog) {
        this(cipherExecutor, ticketSerializationManager, ticketCatalog,
            new ConcurrentHashMap<>(), QueueableTicketRegistryMessagePublisher.noOp(), new PublisherIdentifier());
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog,
                                 final Map<String, Ticket> storageMap,
                                 final QueueableTicketRegistryMessagePublisher ticketPublisher,
                                 final PublisherIdentifier publisherIdentifier) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, ticketPublisher, publisherIdentifier);
        this.mapInstance = storageMap;
    }
}
