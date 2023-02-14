package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
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

    /**
     * A map to contain the tickets.
     */
    private final Map<String, Ticket> mapInstance;

    public DefaultTicketRegistry(final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog) {
        this(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog);
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog) {
        this(cipherExecutor, ticketSerializationManager, ticketCatalog, new ConcurrentHashMap<>());
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog,
                                 final Map<String, Ticket> storageMap) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.mapInstance = storageMap;
    }
}
