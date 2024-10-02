package org.apereo.cas.ticket.registry;

import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the ticket registry that is backed by a map.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Getter
@Monitorable
public class DefaultTicketRegistry extends AbstractMapBasedTicketRegistry {

    private final Map<String, Ticket> mapInstance;

    public DefaultTicketRegistry(final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog, final ConfigurableApplicationContext applicationContext) {
        this(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog, applicationContext);
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog,
                                 final ConfigurableApplicationContext applicationContext) {
        this(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext,
            new ConcurrentHashMap<>(), QueueableTicketRegistryMessagePublisher.noOp(), new PublisherIdentifier());
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog,
                                 final ConfigurableApplicationContext applicationContext,
                                 final Map<String, Ticket> storageMap,
                                 final QueueableTicketRegistryMessagePublisher ticketPublisher,
                                 final PublisherIdentifier publisherIdentifier) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext, ticketPublisher, publisherIdentifier);
        this.mapInstance = storageMap;
    }
}
