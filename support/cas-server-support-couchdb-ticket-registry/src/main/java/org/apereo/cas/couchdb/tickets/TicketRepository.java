package org.apereo.cas.couchdb.tickets;

import org.ektorp.BulkDeleteDocument;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link TicketDocument}. Typed interface to CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@View(name = "all", map = "function(doc) { emit(null, doc._id) }")
public class TicketRepository extends CouchDbRepositorySupport<TicketDocument> {
    public TicketRepository(final CouchDbConnector db) {
        this(db, true);
    }

    public TicketRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(TicketDocument.class, db, createIfNotExists);
    }

    /**
     * Delete tickets.
     *
     * @param ticketDocuments tickets to be deleted.
     * @return number of tickets deleted
     */
    public long delete(final List<TicketDocument> ticketDocuments) {
        return db.executeBulk(ticketDocuments.stream()
            .map(BulkDeleteDocument::of)
            .collect(Collectors.toList()))
            .size();
    }

    /**
     * Gets current document revision.
     *
     * @param id Document id to get revision.
     * @return current document revision.
     */
    public String getCurrentRevision(final String id) {
        return db.getCurrentRevision(id);
    }
}
