package org.apereo.cas.ticket.registry;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link AbstractMapBasedTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractMapBasedTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMapBasedTicketRegistry.class);

    public AbstractMapBasedTicketRegistry() {
    }

    /**
     * Creates a new, empty registry with the cipher.
     *
     * @param cipherExecutor   the cipher executor
     */
    public AbstractMapBasedTicketRegistry(final CipherExecutor cipherExecutor) {
        setCipherExecutor(cipherExecutor);
    }

    @Override
    public void addTicket(final Ticket ticket) {
        Assert.notNull(ticket, "ticket cannot be null");
        final Ticket encTicket = encodeTicket(ticket);
        LOGGER.debug("Added ticket [{}] to registry.", ticket.getId());
        getMapInstance().put(encTicket.getId(), encTicket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }
        final Ticket found = getMapInstance().get(encTicketId);
        final Ticket result = decodeTicket(found);
        if (result != null && result.isExpired()) {
            LOGGER.debug("Ticket [{}] has expired and is now removed from the cache", result.getId());
            getMapInstance().remove(encTicketId);
            return null;
        }
        return result;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (encTicketId == null) {
            return false;
        }
        return getMapInstance().remove(encTicketId) != null;
    }

    @Override
    public long deleteAll() {
        final int size = getMapInstance().size();
        getMapInstance().clear();
        return size;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return decodeTickets(getMapInstance().values());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    /**
     * Create map instance, which must ben created during initialization phases
     * and always be the same instance.
     *
     * @return the map
     */
    public abstract Map<String, Ticket> getMapInstance();
}
