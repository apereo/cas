package org.apereo.cas.ticket.registry;

import org.apereo.cas.couchdb.tickets.TicketDocument;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbTicketRegistry }.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class CouchDbTicketRegistry extends AbstractTicketRegistry {

    private final TicketRepository couchDb;

    private final int conflictRetries;

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketIdToDelete);
        var exception = (DbAccessException) null;
        var success = false;
        val ticketDocument = new TicketDocument();
        ticketDocument.setRevision(couchDb.getCurrentRevision(ticketId));
        ticketDocument.setId(ticketId);
        for (var retries = 0; retries < conflictRetries && exception == null && !success; retries++) {
            try {
                couchDb.remove(ticketDocument);
                success = true;
            } catch (final UpdateConflictException e) {
                ticketDocument.setRevision(couchDb.getCurrentRevision(ticketId));
                if (retries + 1 == conflictRetries) {
                    exception = e;
                }
            } catch (final DocumentNotFoundException e) {
                exception = e;
            }
        }

        if (exception != null) {
            LOGGER.warn("Could not delete [{}] [{}]", ticketId, exception.getMessage());
        } else if (success) {
            LOGGER.trace("Successfully deleted ticket [{}].", ticketId);
        } else {
            LOGGER.warn("Could not delete [{}] - failed.", ticketId);
        }

        return success;
    }

    @Override
    public void addTicketInternal(final Ticket ticketToAdd) {
        val encodedTicket = encodeTicket(ticketToAdd);
        LOGGER.trace("Adding ticket [{}]", encodedTicket.getId());
        couchDb.add(new TicketDocument(encodedTicket));
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.trace("Locating ticket id [{}]", ticketId);
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            LOGGER.trace("Ticket id [{}] could not be found", encTicketId);
            return null;
        }

        try {
            val document = this.couchDb.get(encTicketId);
            val t = document.getTicket();
            LOGGER.trace("Got ticket [{}] from the registry.", t);

            val decoded = decodeTicket(t);
            if (predicate.test(decoded)) {
                return decoded;
            }
            return null;
        } catch (final DocumentNotFoundException ignored) {
            LOGGER.trace("Ticket [{}] not found in the registry.", encTicketId);
        }
        return null;
    }

    @Override
    public long deleteAll() {
        return couchDb.delete(couchDb.getAll());
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(couchDb.getAll().stream().map(TicketDocument::getTicket).collect(Collectors.toList()));
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        val encodedTicket = encodeTicket(ticket);
        LOGGER.trace("Updating [{}]", encodedTicket.getId());
        var success = false;
        val doc = new TicketDocument(encodedTicket);
        doc.setRevision(couchDb.getCurrentRevision(encodedTicket.getId()));
        for (var retries = 0; retries < conflictRetries; retries++) {
            try {
                couchDb.update(doc);
                success = true;
            } catch (final DbAccessException e) {
                doc.setRevision(couchDb.getCurrentRevision(encodedTicket.getId()));
                LOGGER.warn("Could not update [{}] [{}]", encodedTicket.getId(), e.getMessage());
            }
            if (success) {
                LOGGER.trace("Successfully updated ticket [{}].", encodedTicket.getId());
                return ticket;
            }
        }
        return null;
    }
}
