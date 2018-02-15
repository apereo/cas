package org.apereo.cas.ticket.registry.support;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

/**
 * Simple implentation of the JpaTgtDeleteHandler that takes a ticket prefix and
 * looks up the ticket definition.
 * 
 * @author sbearcsiro
 * @since 5.2.3
 *
 */
public class DefaultJpaTgtDeleteHandler implements JpaTgtDeleteHandler {

    private static final String DEFAULT_TICKET_GRANTING_TICKET_FIELD_NAME = "ticketGrantingTicket";

    private final TicketCatalog ticketCatalog;
    private final String prefix;
    private final String ticketGrantingTicketFieldName;

    public DefaultJpaTgtDeleteHandler(final TicketCatalog ticketCatalog, final String prefix) {
        this(ticketCatalog, prefix, DEFAULT_TICKET_GRANTING_TICKET_FIELD_NAME);
    }

    public DefaultJpaTgtDeleteHandler(final TicketCatalog ticketCatalog, final String prefix,
            final String ticketGrantingTicketFieldName) {
        this.ticketCatalog = ticketCatalog;
        this.prefix = prefix;
        this.ticketGrantingTicketFieldName = ticketGrantingTicketFieldName;
    }

    @Override
    public int deleteSingleTgt(final EntityManager entityManager, final String ticketId) {
        final TicketDefinition ot = ticketCatalog.find(prefix);
        final String entityName = ot.getImplementationClass().getSimpleName();
        final Query query = entityManager
                .createQuery("delete from " + entityName + " c where c." + ticketGrantingTicketFieldName + ".id = :id");
        query.setParameter("id", ticketId);
        return query.executeUpdate();
    }

}
