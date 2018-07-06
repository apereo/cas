package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

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
     * @param cipherExecutor the cipher executor
     */
    public AbstractMapBasedTicketRegistry(final CipherExecutor cipherExecutor) {
        setCipherExecutor(cipherExecutor);
    }

    @Override
    public void addTicket(@NonNull final Ticket ticket) {
        val encTicket = encodeTicket(ticket);
        LOGGER.debug("Added ticket [{}] to registry.", ticket.getId());
        getMapInstance().put(encTicket.getId(), encTicket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }
        val found = getMapInstance().get(encTicketId);
        if (found == null) {
            LOGGER.debug("Ticket  [{}] could not be found", encTicketId);
            return null;
        }

        val result = decodeTicket(found);
        if (result != null && result.isExpired()) {
            LOGGER.debug("Ticket [{}] has expired and is now removed from the cache", result.getId());
            getMapInstance().remove(encTicketId);
            return null;
        }
        return result;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            return false;
        }
        return getMapInstance().remove(encTicketId) != null;
    }

    @Override
    public long deleteAll() {
        val size = getMapInstance().size();
        getMapInstance().clear();
        return size;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
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
