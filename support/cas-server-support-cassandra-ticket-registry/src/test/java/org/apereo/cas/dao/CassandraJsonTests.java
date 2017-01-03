package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
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
 * @since 5.1.0
 */
public class CassandraJsonTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraDao<String> dao;

    @Before
    public void setUp() throws Exception {
        dao = new CassandraDao<>("localhost", "", "", new JacksonJsonSerializer(), String.class);
    }

    @Test
    public void shouldWorkWithAStringSerializer() throws Exception {
        final TicketGrantingTicketImpl tgt = TicketCreator.defaultTGT("id");

        dao.addTicketGrantingTicket(tgt);

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }

    @Ignore("To be completed")
    @Test
    public void shouldReturnExpiredTGTs() throws Exception {
        //given
        final TicketGrantingTicketImpl firstExpired = TicketCreator.expiredTGT("expired1");
        final TicketGrantingTicketImpl secondExpired = TicketCreator.expiredTGT("expired2");
        final TicketGrantingTicketImpl notExpired = TicketCreator.defaultTGT("notExpired");

        dao.addTicketGrantingTicket(firstExpired);
        dao.addTicketGrantingTicket(secondExpired);
        dao.addTicketGrantingTicket(notExpired);

        //when
        final Stream<TicketGrantingTicket> expiredTgts = dao.getExpiredTgts();

        //then
        final long expiredTgtsInserted = 2;
        assertThat(expiredTgts.count(), is(expiredTgtsInserted));
    }
}
