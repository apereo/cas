package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.SchedulingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class, locations = {"classpath:HazelcastTicketRegistryTests-context.xml"})
@SpringBootTest(classes = {
        HazelcastTicketRegistryReplicationTests.HazelcastTestConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        HazelcastTicketRegistryTicketCatalogConfiguration.class,
        CasCoreTicketCatalogConfiguration.class})
public class HazelcastTicketRegistryReplicationTests {
    private static final String TGT_ID = "TGT";
    private static final String ST_ID_1 = "ST1";
    private static final String PGT_ID_1 = "PGT-1";
    @Autowired
    @Qualifier("hzTicketRegistry1")
    private TicketRegistry hzTicketRegistry1;

    @Autowired
    @Qualifier("hzTicketRegistry2")
    private TicketRegistry hzTicketRegistry2;

    public void setHzTicketRegistry1(final HazelcastTicketRegistry hzTicketRegistry1) {
        this.hzTicketRegistry1 = hzTicketRegistry1;
    }

    public void setHzTicketRegistry2(final HazelcastTicketRegistry hzTicketRegistry2) {
        this.hzTicketRegistry2 = hzTicketRegistry2;
    }

    @TestConfiguration
    public static class HazelcastTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

    @Test
    public void retrieveCollectionOfTickets() {
        Collection<Ticket> col = this.hzTicketRegistry1.getTickets();
        col.forEach(ticket -> this.hzTicketRegistry1.deleteTicket(ticket.getId()));

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
        assertEquals(1, this.hzTicketRegistry1.deleteTicket("ST-TEST"));
        assertNull(this.hzTicketRegistry1.getTicket("ST-TEST"));
        assertNull(this.hzTicketRegistry2.getTicket("ST-TEST"));
    }

    @Test
    public void verifyDeleteTicketWithChildren() throws Exception {
        this.hzTicketRegistry1.addTicket(new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.hzTicketRegistry1.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(ST_ID_1, service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st2 = tgt.grantServiceTicket("ST2", service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st3 = tgt.grantServiceTicket("ST3", service, new NeverExpiresExpirationPolicy(), false, false);

        this.hzTicketRegistry1.addTicket(st1);
        this.hzTicketRegistry1.addTicket(st2);
        this.hzTicketRegistry1.addTicket(st3);
        this.hzTicketRegistry1.updateTicket(tgt);

        assertNotNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket(ST_ID_1, ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));

        assertTrue("TGT and children were deleted", this.hzTicketRegistry1.deleteTicket(tgt.getId()) > 0);

        assertNull(this.hzTicketRegistry1.getTicket(tgt.getId(), TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket(ST_ID_1, ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST2", ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket("ST3", ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        this.hzTicketRegistry1.addTicket(new TicketGrantingTicketImpl(TGT_ID, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.hzTicketRegistry1.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(ST_ID_1, service, new NeverExpiresExpirationPolicy(), false, true);

        this.hzTicketRegistry1.addTicket(st1);

        assertNotNull(this.hzTicketRegistry1.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNotNull(this.hzTicketRegistry1.getTicket(ST_ID_1, ServiceTicket.class));

        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket(PGT_ID_1, a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.hzTicketRegistry1.addTicket(pgt);
        this.hzTicketRegistry1.updateTicket(tgt);
        assertSame(3, this.hzTicketRegistry1.deleteTicket(tgt.getId()));

        assertNull(this.hzTicketRegistry1.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket(ST_ID_1, ServiceTicket.class));
        assertNull(this.hzTicketRegistry1.getTicket(PGT_ID_1, ProxyGrantingTicket.class));
    }

    private static TicketGrantingTicket newTestTgt() {
        return new MockTicketGrantingTicket("casuser");
    }

    private static ServiceTicket newTestSt(final TicketGrantingTicket tgt) {
        return new MockServiceTicket("ST-TEST", RegisteredServiceTestUtils.getService(), tgt);
    }
}
