package org.apereo.cas.couchdb.tickets;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.ticket.Ticket;
import org.ektorp.support.CouchDbDocument;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link TicketDocument}. Wraps a {@link Ticket} for use with CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@NoArgsConstructor
public class TicketDocument extends CouchDbDocument {
    private Ticket ticket;

    public TicketDocument(final Ticket ticket) {
        this.setId(ticket.getId());
        this.ticket = ticket;
    }

    public TicketDocument(final String id, final String rev) {
        this.setId(id);
        this.setRevision(rev);
    }
}
