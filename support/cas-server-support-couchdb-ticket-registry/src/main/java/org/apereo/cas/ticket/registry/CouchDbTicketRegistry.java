package org.apereo.cas.ticket.registry;

import org.apereo.cas.couchdb.tickets.TicketDocument;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;

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
 * @deprecated Since 7
 */
@Slf4j
@Deprecated(since = "7.0.0")
public class CouchDbTicketRegistry extends AbstractTicketRegistry {

    private final TicketRepository couchDb;

    private final int conflictRetries;

    public CouchDbTicketRegistry(final CipherExecutor cipherExecutor, final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog, final TicketRepository couchDb, final int conflictRetries) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.couchDb = couchDb;
        this.conflictRetries = conflictRetries;
    }

    @Override
    public long deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = digest(ticketIdToDelete);
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

        return success ? 1 : 0;
    }

    @Override
    public void addTicketInternal(final Ticket ticketToAdd) throws Exception {
        val encodedTicket = encodeTicket(ticketToAdd);
        LOGGER.trace("Adding ticket [{}]", encodedTicket.getId());
        couchDb.add(new TicketDocument(encodedTicket));
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.trace("Locating ticket id [{}]", ticketId);
        val encTicketId = digest(ticketId);
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
    public Ticket updateTicket(final Ticket ticket) throws Exception {
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
