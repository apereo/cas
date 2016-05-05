package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
public class HazelcastTicketRegistryTests {
    private HazelcastTicketRegistry hzTicketRegistry1;

    private HazelcastTicketRegistry hzTicketRegistry2;

    @Before
    public void before() {
        final ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:HazelcastTicketRegistryTests-context.xml");
        this.hzTicketRegistry1 = ctx.getBean("hzTicketRegistry1", HazelcastTicketRegistry.class);
        this.hzTicketRegistry2 = ctx.getBean("hzTicketRegistry2", HazelcastTicketRegistry.class);
    }

    @After
    public void after() {
        this.hzTicketRegistry1.shutdown();
        this.hzTicketRegistry2.shutdown();
    }

    public void setHzTicketRegistry1(final HazelcastTicketRegistry hzTicketRegistry1) {
        this.hzTicketRegistry1 = hzTicketRegistry1;
    }

    public void setHzTicketRegistry2(final HazelcastTicketRegistry hzTicketRegistry2) {
        this.hzTicketRegistry2 = hzTicketRegistry2;
    }

    @Test
    public void retrieveCollectionOfTickets() {
        Collection<Ticket> col = this.hzTicketRegistry1.getTickets();
        for (final Ticket ticket : col) {
            this.hzTicketRegistry1.deleteTicket(ticket.getId());
        }

        col = hzTicketRegistry2.getTickets();
        assertEquals(0, col.size());

        final TicketGrantingTicket tgt = newTestTgt();
        this.hzTicketRegistry1.addTicket(tgt);

        this.hzTicketRegistry1.addTicket(newTestSt(tgt));

        col = hzTicketRegistry2.getTickets();
        assertEquals(2, col.size());
        assertEquals(1, hzTicketRegistry2.serviceTicketCount());
        assertEquals(1, hzTicketRegistry2.sessionCount());
    }

    @Test
    public void basicOperationsAndClustering() throws Exception {
        final TicketGrantingTicket tgt = newTestTgt();
        this.hzTicketRegistry1.addTicket(tgt);

        assertNotNull(this.hzTicketRegistry1.getTicket(tgt.getId()));
        assertNotNull(this.hzTicketRegistry2.getTicket(tgt.getId()));
        assertTrue(this.hzTicketRegistry2.deleteTicket(tgt.getId()));
        assertFalse(this.hzTicketRegistry1.deleteTicket(tgt.getId()));
        assertNull(this.hzTicketRegistry1.getTicket(tgt.getId()));
        assertNull(this.hzTicketRegistry2.getTicket(tgt.getId()));

        final ServiceTicket st = newTestSt(tgt);
        this.hzTicketRegistry2.addTicket(st);

        assertNotNull(this.hzTicketRegistry1.getTicket("ST-TEST"));
        assertNotNull(this.hzTicketRegistry2.getTicket("ST-TEST"));
        this.hzTicketRegistry1.deleteTicket("ST-TEST");
        assertNull(this.hzTicketRegistry1.getTicket("ST-TEST"));
        assertNull(this.hzTicketRegistry2.getTicket("ST-TEST"));
    }

    @Test
    public void verifyDeleteTicketWithChildren() throws Exception {
        this.hzTicketRegistry1.addTicket(new TicketGrantingTicketImpl(
                "TGT", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.hzTicketRegistry1.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = org.apereo.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st2 = tgt.grantServiceTicket(
                "ST2", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st3 = tgt.grantServiceTicket(
                "ST3", service, new NeverExpiresExpirationPolicy(), true, false);

        this.hzTicketRegistry1.addTicket(st1);
        this.hzTicketRegistry1.addTicket(st2);
        this.hzTicketRegistry1.addTicket(st3);
        this.hzTicketRegistry1.updateTicket(tgt);
        
        assertNotNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));

        this.hzTicketRegistry1.deleteTicket(tgt.getId());

        assertNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));
    }

    private TicketGrantingTicket newTestTgt() {
        return new MockTicketGrantingTicket("casuser");
    }

    private ServiceTicket newTestSt(final TicketGrantingTicket tgt) {
        return new MockServiceTicket("ST-TEST", org.apereo.cas.services.TestUtils.getService(), tgt);
    }


}
