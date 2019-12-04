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
        try {
            ticketDocument.setRevision(couchDb.getCurrentRevision(ticketId));
        } catch (final DocumentNotFoundException e) {
            exception = e;
        }
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
            LOGGER.debug("Could not delete [{}] [{}]", ticketId, exception.getMessage());
        } else if (success) {
            LOGGER.debug("Successfully deleted ticket [{}].", ticketId);
        } else {
            LOGGER.debug("Could not delete [{}] - failed.", ticketId);
        }

        return success;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val encodedTicket = encodeTicket(ticketToAdd);
        LOGGER.debug("Adding ticket [{}]", encodedTicket.getId());
        couchDb.add(new TicketDocument(encodedTicket));
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.debug("Locating ticket id [{}]", ticketId);
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            LOGGER.debug("Ticket id [{}] could not be found", encTicketId);
            return null;
        }

        try {
            val document = this.couchDb.get(encTicketId);
            val t = document.getTicket();
            LOGGER.debug("Got ticket [{}] from the registry.", t);

            val decoded = decodeTicket(t);
            if (predicate.test(decoded)) {
                return decoded;
            }
            return null;
        } catch (final DocumentNotFoundException ignored) {
            LOGGER.debug("Ticket [{}] not found in the registry.", encTicketId);
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
        LOGGER.debug("Updating [{}]", encodedTicket.getId());
        var exception = (DbAccessException) null;
        var success = false;
        val doc = new TicketDocument(encodedTicket);
        doc.setRevision(couchDb.getCurrentRevision(encodedTicket.getId()));
        for (var retries = 0; retries < conflictRetries; retries++) {
            try {
                exception = null;
                couchDb.update(doc);
                success = true;
            } catch (final DbAccessException e) {
                doc.setRevision(couchDb.getCurrentRevision(encodedTicket.getId()));
                exception = e;
            }
            if (success) {
                LOGGER.debug("Successfully updated ticket [{}].", encodedTicket.getId());
                return ticket;
            }
        }
        if (exception != null) {
            LOGGER.debug("Could not update [{}] [{}]", encodedTicket.getId(), exception.getMessage());
        }
        return null;
    }
}
