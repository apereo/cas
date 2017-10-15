package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the TicketRegistry that is backed by a ConcurrentHashMap.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultTicketRegistry extends AbstractMapBasedTicketRegistry {

    /**
     * A map to contain the tickets.
     */
    private final Map<String, Ticket> cache;

    public DefaultTicketRegistry() {
        this(NoOpCipherExecutor.getInstance());
    }

    public DefaultTicketRegistry(final CipherExecutor cipherExecutor) {
        super(cipherExecutor);
        this.cache = new ConcurrentHashMap<>();
    }

    public DefaultTicketRegistry(final int initialCapacity, final int loadFactor,
                                 final int concurrencyLevel,
                                 final CipherExecutor cipherExecutor) {
        super(cipherExecutor);
        this.cache = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public Map<String, Ticket> getMapInstance() {
        return this.cache;
    }
}
