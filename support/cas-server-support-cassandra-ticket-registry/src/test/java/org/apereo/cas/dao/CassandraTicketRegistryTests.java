package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreatorUtils;
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
public class CassandraTicketRegistryTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraTicketRegistry<String> ticketRegistry;

    @Before
    public void setUp() throws Exception {
        ticketRegistry = new CassandraTicketRegistry<>("localhost", "", "", new JacksonJsonSerializer(), String.class, "cas2.ticketgrantingticket",
                "cas2.serviceticket", "cas2.ticket_cleaner", "cas2.ticket_cleaner_lastrun");
    }

    @Test
    public void shouldRetrieveATicket() throws Exception {
        final String ticketId = "TGT-1234";
        final TicketGrantingTicketImpl ticket = TicketCreatorUtils.defaultTGT(ticketId);
        ticketRegistry.addTicket(ticket);

        assertEquals(ticket, ticketRegistry.getTicket(ticketId));
    }
}
