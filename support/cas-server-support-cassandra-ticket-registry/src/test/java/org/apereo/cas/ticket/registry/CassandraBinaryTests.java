package org.apereo.cas.ticket.registry;

import org.apereo.cas.serializer.JacksonBinarySerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCatalogUtils;
import org.apereo.cas.utils.TicketCreatorUtils;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class CassandraBinaryTests {

    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {
        final CassandraTicketRegistry<ByteBuffer> dao = new CassandraTicketRegistry<>(TicketCatalogUtils.getTicketCatalog(), "localhost", "", "", "cas_binary",
                new JacksonBinarySerializer(), ByteBuffer.class);

        final TicketGrantingTicketImpl ticket = TicketCreatorUtils.defaultTGT("TGT-id");

        dao.addTicket(ticket);

        assertEquals(ticket, dao.getTicket("TGT-id"));
    }
}
