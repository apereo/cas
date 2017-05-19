package org.apereo.cas.ticket.registry;

import org.apereo.cas.serializer.JacksonBinarySerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCreatorUtils;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Rule;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 *
 * @since 5.1.0
 */
public class CassandraBinaryTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema-binary.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {
        final CassandraTicketRegistry<ByteBuffer> dao = new CassandraTicketRegistry<>("localhost", "", "", new JacksonBinarySerializer(), ByteBuffer.class,
                "cas_binary.ticketgrantingticket", "cas_binary.serviceticket", "cas_binary.ticket_cleaner", "cas_binary.ticket_cleaner_lastrun");

        final TicketGrantingTicketImpl tgt = TicketCreatorUtils.defaultTGT("id");

        dao.addTicketGrantingTicket(tgt);

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }
}
