package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 *
 * @since 5.1.0
 */
public class NoSqlTicketRegistryTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraDao<String> dao;

    @Before
    public void setUp() throws Exception {
        dao = new CassandraDao<>("localhost", "", "", new JacksonJsonSerializer(), String.class, "cas.ticketgrantingticket",
                "cas.serviceticket", "cas.ticket_cleaner", "cas.ticket_cleaner_lastrun");
    }

    @Test
    public void shouldRetrieveATicket() throws Exception {
        final NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao);
        final String ticketId = "TGT-1234";
        final TicketGrantingTicketImpl ticket = TicketCreator.defaultTGT(ticketId);
        ticketRegistry.addTicket(ticket);

        assertEquals(ticket, ticketRegistry.getTicket(ticketId));
    }
}
