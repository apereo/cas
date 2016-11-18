package org.apereo.cas.dao;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.serializer.JacksonJSONSerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, mock(LogoutManager.class));
        String ticketId = "TGT-1234";
        TicketGrantingTicketImpl ticket = TicketCreator.defaultTGT(ticketId);
        ticketRegistry.addTicket(ticket);

        assertEquals(ticket, ticketRegistry.getTicket(ticketId));
    }

    @Test
    public void shouldLogUserOutOfServices() throws Exception {
        LogoutManager logoutManager = mock(LogoutManager.class);
        NoSqlTicketRegistryDao dao = mock(NoSqlTicketRegistryDao.class);
        NoSqlTicketRegistry ticketRegistry = new NoSqlTicketRegistry(dao, logoutManager);

        when(dao.getExpiredTgts()).thenReturn(Stream.of(TicketCreator.expiredTGT("expiredId")));

        //when
        ticketRegistry.clean();

        //then
        verify(logoutManager).performLogout(any());
    }
}