package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.couchdb.tickets.TicketDocument;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CouchDbTicketRegistry }.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@AllArgsConstructor
@Slf4j
public class CouchDbTicketRegistry extends AbstractTicketRegistry {

    private final TicketCatalog ticketCatalog;
    private final TicketRepository couchDb;
    private final int conflictRetries;

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        final var ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketIdToDelete);
        DbAccessException exception = null;
        var success = false;
        final var ticketDocument = new TicketDocument();
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
                // Retry if update conflict.
                ticketDocument.setRevision(couchDb.getCurrentRevision(ticketId));
                if (retries + 1 == conflictRetries) {
                    exception = e;
                }
            } catch (final DocumentNotFoundException e) {
                // Fail if document not found.
                exception = e;
            }
        }

        if (exception != null) {
            LOGGER.debug("Could not delete [{}] {}", ticketId, exception.getMessage());
        } else if (success) {
            LOGGER.debug("Successfully deleted ticket [{}].", ticketId);
        } else {
            LOGGER.debug("Could not delete [{}] - failed.", ticketId);
        }

        return success;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final var encodedTicket = encodeTicket(ticketToAdd);
        LOGGER.debug("Adding ticket [{}]", encodedTicket.getId());

        couchDb.add(new TicketDocument(encodedTicket));
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        LOGGER.debug("Locating ticket id [{}]", ticketId);
        final var encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            LOGGER.debug("Ticket id [{}] could not be found", encTicketId);
            return null;
        }

        TicketDocument document;

        try {
            document = this.couchDb.get(encTicketId);
        } catch (final DocumentNotFoundException ignored) {
            document = null;
        }

        if (document != null) {
            final var t = document.getTicket();
            LOGGER.debug("Got ticket [{}] from the registry.", t);

            final var decoded = decodeTicket(t);
            if (decoded == null || decoded.isExpired()) {
                LOGGER.warn("The expiration policy for ticket id [{}] has expired the ticket", encTicketId);
                return null;
            }
            return decoded;
        }
        LOGGER.debug("Ticket [{}] not found in the registry.", encTicketId);
        return null;
    }

    @Override
    public long deleteAll() {
        return couchDb.delete(couchDb.getAll());
    }


    @Override
    public Collection<Ticket> getTickets() {
        return decodeTickets(couchDb.getAll().stream().map(TicketDocument::getTicket).collect(Collectors.toList()));
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        final var encodedTicket = encodeTicket(ticket);
        LOGGER.debug("Updating [{}]", encodedTicket.getId());
        DbAccessException exception = null;
        var success = false;
        final var doc = new TicketDocument(encodedTicket);
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
            LOGGER.debug("Could not update [{}] {}", encodedTicket.getId(), exception.getMessage());
        }
        return null;
    }

    @Override
    public Stream<Ticket> getTicketsStream() {
        return getTickets().stream();
    }
}
