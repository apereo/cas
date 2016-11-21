package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonJSONSerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoSqlTicketRegistryTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraDao<String> dao;

    @Before
    public void setUp() throws Exception {
        dao = new CassandraDao<>("localhost", "", "", new JacksonJSONSerializer(), String.class);
    }

    @Test
    public void shouldRetrieveATicket() throws Exception {
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao);
        String ticketId = "TGT-1234";
        TicketGrantingTicketImpl ticket = TicketCreator.defaultTGT(ticketId);
        ticketRegistry.addTicket(ticket);

        assertEquals(ticket, ticketRegistry.getTicket(ticketId));
    }
}