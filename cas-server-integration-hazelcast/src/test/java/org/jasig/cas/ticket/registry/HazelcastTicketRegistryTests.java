package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.TestUtils;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void basicOperationsAndClustering() throws Exception {
        this.hzTicketRegistry1.addTicket(newTestTgt());

        assertNotNull(this.hzTicketRegistry1.getTicket("TGT-TEST"));
        assertNotNull(this.hzTicketRegistry2.getTicket("TGT-TEST"));
        assertTrue(this.hzTicketRegistry2.deleteTicket("TGT-TEST"));
        assertFalse(this.hzTicketRegistry1.deleteTicket("TGT-TEST"));
        assertNull(this.hzTicketRegistry1.getTicket("TGT-TEST"));
        assertNull(this.hzTicketRegistry2.getTicket("TGT-TEST"));

        final ServiceTicket st = newTestSt();
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

        assertNotNull(this.hzTicketRegistry1.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));

        this.hzTicketRegistry1.deleteTicket(tgt.getId());

        assertNull(this.hzTicketRegistry1.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST1", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));
    }

    private TicketGrantingTicket newTestTgt() {
        return new MockTgt();
    }

    private ServiceTicket newTestSt() {
        return new MockSt();
    }

    private static class MockTgt implements TicketGrantingTicket {

        @Override
        public Service getProxiedBy() {
            return null;
        }

        @Override
        public Authentication getAuthentication() {
            return null;
        }

        @Override
        public List<Authentication> getSupplementalAuthentications() {
            return null;
        }

        @Override
        public ServiceTicket grantServiceTicket(final String id,
                                                final Service service,
                                                final ExpirationPolicy expirationPolicy,
                                                final boolean credentialsProvided,
                                                final boolean onlyTrackMostRecentSession) {
            return null;
        }

        @Override
        public Map<String, Service> getServices() {
            return null;
        }

        @Override
        public void removeAllServices() {

        }

        @Override
        public void markTicketExpired() {

        }

        @Override
        public boolean isRoot() {
            return false;
        }

        @Override
        public TicketGrantingTicket getRoot() {
            return null;
        }

        @Override
        public List<Authentication> getChainedAuthentications() {
            return null;
        }

        @Override
        public String getId() {
            return "TGT-TEST";
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public TicketGrantingTicket getGrantingTicket() {
            return this;
        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public int getCountOfUses() {
            return 0;
        }
    }

    private static class MockSt implements ServiceTicket {
        private static final long serialVersionUID = -761672450629794769L;

        @Override
        public Service getService() {
            return null;
        }

        @Override
        public boolean isFromNewLogin() {
            return false;
        }

        @Override
        public boolean isValidFor(final Service service) {
            return false;
        }

        @Override
        public ProxyGrantingTicket grantProxyGrantingTicket(final String id,
                                                            final Authentication authentication,
                                                            final ExpirationPolicy expirationPolicy) {
            return null;
        }

        @Override
        public String getId() {
            return "ST-TEST";
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public TicketGrantingTicket getGrantingTicket() {
            return null;
        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public int getCountOfUses() {
            return 0;
        }
    }
}
