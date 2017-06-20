package org.apereo.cas.ticket.registry;

import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreatorUtils;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class CassandraJsonTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraTicketRegistry<String> dao;

    @Before
    public void setUp() throws Exception {
        final DefaultTicketCatalog ticketCatalog = new DefaultTicketCatalog();
        dao = new CassandraTicketRegistry<>(ticketCatalog, "localhost", "", "", new JacksonJsonSerializer(), String.class, "cas2.ticketgrantingticket",
                "cas2.serviceticket", "cas2.ticket_cleaner", "cas2.ticket_cleaner_lastrun");
    }

    @Test
    public void shouldWorkWithAStringSerializer() throws Exception {
        final TicketGrantingTicketImpl tgt = TicketCreatorUtils.defaultTGT("TGT-id");

        dao.addTicket(tgt);

        assertEquals(tgt, dao.getTicket("TGT-id"));
    }

    @Ignore("To be completed")
    @Test
    public void shouldReturnExpiredTGTs() throws Exception {
        //given
        final TicketGrantingTicketImpl firstExpired = TicketCreatorUtils.expiredTGT("expired1");
        final TicketGrantingTicketImpl secondExpired = TicketCreatorUtils.expiredTGT("expired2");
        final TicketGrantingTicketImpl notExpired = TicketCreatorUtils.defaultTGT("notExpired");

        dao.addTicket(firstExpired);
        dao.addTicket(secondExpired);
        dao.addTicket(notExpired);

        //when
        final Stream<Ticket> expiredTgts = dao.getExpiredTgts();

        //then
        final long expiredTgtsInserted = 2;
        assertThat(expiredTgts.count(), is(expiredTgtsInserted));
    }
}
