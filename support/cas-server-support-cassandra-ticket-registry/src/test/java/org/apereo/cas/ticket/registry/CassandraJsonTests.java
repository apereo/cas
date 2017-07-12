package org.apereo.cas.ticket.registry;

import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.utils.TicketCatalogUtils;
import org.apereo.cas.utils.TicketCreatorUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public class CassandraJsonTests {

    private CassandraTicketRegistry<String> dao;

    @Before
    public void setUp() throws Exception {
        dao = new CassandraTicketRegistry<>(TicketCatalogUtils.getTicketCatalog(), "localhost", "", "", "cas2", new JacksonJsonSerializer(), String.class);
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
        final Collection<Ticket> expiredTgts = dao.getTickets();

        //then
        final long expiredTgtsInserted = 2;
        assertThat(expiredTgts.size(), is(expiredTgtsInserted));
    }
}
