package org.apereo.cas.ticket.registry;

import org.apereo.cas.AbstractMemcachedTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for MemCacheTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
public class MemCacheTicketRegistryTests extends AbstractMemcachedTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemCacheTicketRegistryTests.class);
    private static final String TGT_ID = "TGT";
    private static final String ST_1_ID = "ST1";
    private static final String PGT_1_ID = "PGT-1";

    private MemCacheTicketRegistry registry;
    private final String registryBean;

    public MemCacheTicketRegistryTests(final String beanName) {
        registryBean = beanName;
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        return Arrays.asList(new Object[]{"testCase1"}, new Object[]{"testCase2"});
    }


    @Before
    public void setUp() throws IOException {
        final boolean environmentOk = isMemcachedListening();
        if (!environmentOk) {
            LOGGER.warn("Aborting test since no memcached server is available on localhost.");
        }
        Assume.assumeTrue(environmentOk);
        final ApplicationContext context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
        registry = context.getBean(registryBean, MemCacheTicketRegistry.class);
    }

    @Test
    public void verifyWriteGetDelete() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL123-crud";
        final ServiceTicket ticket = new MockServiceTicket(id, RegisteredServiceTestUtils.getService(),
                new MockTicketGrantingTicket("test"));
        registry.addTicket(ticket);
        final ServiceTicket ticketFromRegistry = (ServiceTicket) registry.getTicket(id);
        assertNotNull(ticketFromRegistry);
        assertEquals(id, ticketFromRegistry.getId());
        registry.deleteTicket(id);
        assertNull(registry.getTicket(id));
    }

    @Test
    public void verifyExpiration() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-exp1";
        final MockServiceTicket ticket = new MockServiceTicket(id, RegisteredServiceTestUtils.getService(), new MockTicketGrantingTicket("test"));
        ticket.setExpiration(new AlwaysExpiresExpirationPolicy());
        registry.addTicket(ticket);
        Thread.sleep(1500);
        assertNull(registry.getTicket(id, ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithChildren() throws Exception {
        this.registry.addTicket(new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.registry.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(ST_1_ID, service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st2 = tgt.grantServiceTicket("ST2", service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st3 = tgt.grantServiceTicket("ST3", service, new NeverExpiresExpirationPolicy(), false, false);

        this.registry.addTicket(st1);
        this.registry.addTicket(st2);
        this.registry.addTicket(st3);
        this.registry.updateTicket(tgt);

        assertNotNull(this.registry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNotNull(this.registry.getTicket(ST_1_ID, ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST3", ServiceTicket.class));

        this.registry.deleteTicket(tgt.getId());

        assertNull(this.registry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNull(this.registry.getTicket(ST_1_ID, ServiceTicket.class));
        assertNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNull(this.registry.getTicket("ST3", ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        this.registry.addTicket(new TicketGrantingTicketImpl(TGT_ID, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.registry.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(ST_1_ID, service, new NeverExpiresExpirationPolicy(), false, true);
        this.registry.addTicket(st1);
        this.registry.updateTicket(tgt);

        assertNotNull(this.registry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNotNull(this.registry.getTicket(ST_1_ID, ServiceTicket.class));

        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket(PGT_1_ID, a, new NeverExpiresExpirationPolicy());
        this.registry.addTicket(pgt);
        this.registry.updateTicket(tgt);
        this.registry.updateTicket(st1);
        assertEquals(pgt.getGrantingTicket(), tgt);
        assertNotNull(this.registry.getTicket(PGT_1_ID, ProxyGrantingTicket.class));
        assertEquals(a, pgt.getAuthentication());
        assertNotNull(this.registry.getTicket(ST_1_ID, ServiceTicket.class));

        assertTrue(this.registry.deleteTicket(tgt.getId()) > 0);

        assertNull(this.registry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNull(this.registry.getTicket(ST_1_ID, ServiceTicket.class));
        assertNull(this.registry.getTicket(PGT_1_ID, ProxyGrantingTicket.class));
    }
}
