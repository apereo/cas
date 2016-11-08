package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonBinarySerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CassandraBinaryTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema-binary.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {
        CassandraDao dao = new CassandraDao("localhost", 24, "", "", 100, new JacksonBinarySerializer());

        TicketGrantingTicketImpl tgt = TicketCreator.defaultTGT();

        dao.addTicketGrantingTicket(tgt);

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }

    @Ignore("To be completed")
    @Test
    public void shouldReturnExpiredTGTs() throws Exception {

    }

}