package org.apereo.cas.ticket.registry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;

import java.util.Collection;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * This is {@link AbstractMapBasedTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractMapBasedTicketRegistry extends AbstractTicketRegistry {

    /**
     * Creates a new, empty registry with the cipher.
     *
     * @param cipherExecutor   the cipher executor
     */
    public AbstractMapBasedTicketRegistry(final CipherExecutor cipherExecutor) {
        setCipherExecutor(cipherExecutor);
    }

    @Override
    public void addTicket(@NonNull final Ticket ticket) {
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
