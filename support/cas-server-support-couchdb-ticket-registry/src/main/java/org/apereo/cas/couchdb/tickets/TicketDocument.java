package org.apereo.cas.couchdb.tickets;

import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ektorp.support.CouchDbDocument;

/**
 * This is {@link TicketDocument}. Wraps a {@link Ticket} for use with CouchDB.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
public class TicketDocument extends CouchDbDocument {
    private static final long serialVersionUID = -5460618381339711000L;

    private Ticket ticket;

    @JsonCreator
    public TicketDocument(@JsonProperty("ticket") final Ticket ticket) {
        this.setId(ticket.getId());
        this.ticket = ticket;
    }
}
