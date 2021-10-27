package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.mock.MockServiceTicket;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.services.TestUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.junit.Assert.*;

import org.jasig.cas.authentication.Authentication;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:HazelcastTicketRegistryTests-context.xml")
public class HazelcastTicketRegistryTests {

    @Autowired
    private HazelcastTicketRegistry hzTicketRegistry1;

    @Autowired
    private HazelcastTicketRegistry hzTicketRegistry2;

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
        assertEquals(1, this.hzTicketRegistry2.deleteTicket(tgt.getId()));
        assertEquals(0, this.hzTicketRegistry1.deleteTicket(tgt.getId()));
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
                "TGT", org.jasig.cas.authentication.TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.hzTicketRegistry1.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st2 = tgt.grantServiceTicket(
                "ST2", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st3 = tgt.grantServiceTicket(
                "ST3", service, new NeverExpiresExpirationPolicy(), true, false);

        this.hzTicketRegistry1.addTicket(st1);
        this.hzTicketRegistry1.addTicket(st2);
        this.hzTicketRegistry1.addTicket(st3);

        assertNotNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));

        assertTrue("TGT and children were deleted", this.hzTicketRegistry1.deleteTicket(tgt.getId()) > 0);

        assertNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = org.jasig.cas.authentication.TestUtils.getAuthentication();
        this.hzTicketRegistry1.addTicket(new TicketGrantingTicketImpl(
                "TGT", a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.hzTicketRegistry1.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = org.jasig.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);

        this.hzTicketRegistry1.addTicket(st1);

        assertNotNull(this.hzTicketRegistry1.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));

        final TicketGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        assertTrue("TGT and children were deleted", this.hzTicketRegistry1.deleteTicket(tgt.getId()) > 0);

        assertNull(this.hzTicketRegistry1.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("PGT-1", ServiceTicket.class));
    }

    private TicketGrantingTicket newTestTgt() {
        return new MockTicketGrantingTicket("casuser");
    }

    private ServiceTicket newTestSt(final TicketGrantingTicket tgt) {
        return new MockServiceTicket("ST-TEST", TestUtils.getService(), tgt);
    }


}
