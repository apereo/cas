package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonJSONSerializer;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CassandraJSONTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);
    private CassandraDao<String> dao;

    @Before
    public void setUp() throws Exception {
        dao = new CassandraDao<>("localhost", 24, "", "", new ExpirationCalculator(0, 0, 0), new JacksonJSONSerializer(), String.class);
    }

    @Test
    public void shouldWorkWithAStringSerializer() throws Exception {
        TicketGrantingTicketImpl tgt = TicketCreator.defaultTGT("id");

        dao.addTicketGrantingTicket(tgt);

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }

    @Ignore("To be completed")
    @Test
    public void shouldReturnExpiredTGTs() throws Exception {
        //given
        TicketGrantingTicketImpl firstExpired = TicketCreator.expiredTGT("expired1");
        TicketGrantingTicketImpl secondExpired = TicketCreator.expiredTGT("expired2");
        TicketGrantingTicketImpl notExpired = TicketCreator.defaultTGT("notExpired");

        dao.addTicketGrantingTicket(firstExpired);
        dao.addTicketGrantingTicket(secondExpired);
        dao.addTicketGrantingTicket(notExpired);

        //when
        Stream<TicketGrantingTicket> expiredTgts = dao.getExpiredTgts();

        //then
        long expiredTgtsInserted = 2;
        assertThat(expiredTgts.count(), is(expiredTgtsInserted));
    }
}