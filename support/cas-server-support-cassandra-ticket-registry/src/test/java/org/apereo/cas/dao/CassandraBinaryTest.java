package org.apereo.cas.dao;

import org.apereo.cas.serializer.JacksonBinarySerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreator;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Rule;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class CassandraBinaryTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema-binary.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {
        CassandraDao<ByteBuffer> dao = new CassandraDao<>("localhost", 24, "", "", new ExpirationCalculator(0, 0, 0), new JacksonBinarySerializer(), ByteBuffer.class);

        TicketGrantingTicketImpl tgt = TicketCreator.defaultTGT("id");

        dao.addTicketGrantingTicket(tgt);

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }
}