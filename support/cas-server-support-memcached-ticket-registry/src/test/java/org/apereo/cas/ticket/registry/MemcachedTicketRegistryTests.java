package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for MemcachedTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {MemcachedTicketRegistryConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/memcached.properties"})
public class MemcachedTicketRegistryTests extends AbstractTicketRegistryTests {

    private static final String TGT_ID = "TGT";
    private static final String ST_1_ID = "ST1";
    private static final String PGT_1_ID = "PGT-1";

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry registry;

    public MemcachedTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return registry;
    }

    
    @Override
    protected boolean isIterableRegistry() {
        return false;
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
