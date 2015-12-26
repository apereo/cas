package org.jasig.cas.ticket.registry;

import org.jasig.cas.AbstractMemcachedTests;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for MemCacheTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
public class MemCacheTicketRegistryTests extends AbstractMemcachedTests {

    private MemCacheTicketRegistry registry;

    private final String registryBean;

    private final boolean binaryProtocol;

    public MemCacheTicketRegistryTests(final String beanName, final boolean binary) {
        registryBean = beanName;
        binaryProtocol = binary;
    }

    @Parameterized.Parameters
    public static Collection getTestParameters() throws Exception {
        return Arrays.asList(new Object[] {"testCase1", false}, new Object[] {"testCase2", true});
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        bootstrap();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        shutdown();
    }

    @Before
    public void setUp() throws IOException {

        // Abort tests if there is no memcached server available on localhost:11211.
        final boolean environmentOk = isMemcachedListening();
        if (!environmentOk) {
            logger.warn("Aborting test since no memcached server is available on localhost.");
        }
        Assume.assumeTrue(environmentOk);
        final ApplicationContext context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
        registry = context.getBean(registryBean, MemCacheTicketRegistry.class);
    }

    @Test
    public void verifyWriteGetDelete() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-crud";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        final ServiceTicket ticketFromRegistry = (ServiceTicket) registry.getTicket(id);
        Assert.assertNotNull(ticketFromRegistry);
        Assert.assertEquals(id, ticketFromRegistry.getId());
        registry.deleteTicket(id);
        Assert.assertNull(registry.getTicket(id));
    }

    @Test
    public void verifyExpiration() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-exp";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        Assert.assertNotNull(registry.getTicket(id, ServiceTicket.class));
        // Sleep a little longer than service ticket expiry defined in Spring context
        Thread.sleep(2100);
        Assert.assertNull(registry.getTicket(id, ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithChildren() throws Exception {
        this.registry.addTicket(new TicketGrantingTicketImpl(
                "TGT",
                org.jasig.cas.authentication.TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.registry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service =
                org.jasig.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st2 = tgt.grantServiceTicket(
                "ST2", service, new NeverExpiresExpirationPolicy(), true, false);
        final ServiceTicket st3 = tgt.grantServiceTicket(
                "ST3", service, new NeverExpiresExpirationPolicy(), true, false);

        this.registry.addTicket(st1);
        this.registry.addTicket(st2);
        this.registry.addTicket(st3);

        assertNotNull(this.registry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.registry.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST3", ServiceTicket.class));

        this.registry.deleteTicket(tgt.getId());

        assertNull(this.registry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.registry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNull(this.registry.getTicket("ST3", ServiceTicket.class));
    }


}
