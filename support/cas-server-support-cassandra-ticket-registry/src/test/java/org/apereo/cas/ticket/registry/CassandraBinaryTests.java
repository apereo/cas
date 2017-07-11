package org.apereo.cas.ticket.registry;

import org.apereo.cas.serializer.JacksonBinarySerializer;
import org.apereo.cas.ticket.DefaultTicketCatalog;
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
 * @since 5.2.0
 */
public class CassandraBinaryTests {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema-binary.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {
        final DefaultTicketCatalog ticketCatalog = new DefaultTicketCatalog();
        final CassandraTicketRegistry<ByteBuffer> dao = new CassandraTicketRegistry<>(ticketCatalog, "localhost", "", "", "cas_binary",
                new JacksonBinarySerializer(), ByteBuffer.class);

        final TicketGrantingTicketImpl tgt = TicketCreatorUtils.defaultTGT("TGT-id");

        dao.addTicket(tgt);

        assertEquals(tgt, dao.getTicket("TGT-id"));
    }
}
