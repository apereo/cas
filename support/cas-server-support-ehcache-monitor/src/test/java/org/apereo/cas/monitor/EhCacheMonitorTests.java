package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.monitor.config.EhCacheMonitorConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for {@link EhCacheMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        EhcacheTicketRegistryConfiguration.class,
        EhcacheTicketRegistryTicketCatalogConfiguration.class,
        EhCacheMonitorConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreHttpConfiguration.class
})
@TestPropertySource(locations = {"classpath:/ehcache.properties"})
public class EhCacheMonitorTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ehcacheMonitor")
    private Monitor monitor;

    @Test
    public void verifyObserve() {
        CacheStatus status = CacheStatus.class.cast(monitor.observe());
        CacheStatistics stats = getServiceTicketStats(status);

        assertEquals(100, stats.getCapacity());
        assertEquals(0, stats.getSize());
        assertEquals(StatusCode.OK, status.getCode());

        // Fill cache 95% full, which is above 10% free WARN threshold
        IntStream.range(0, 95).forEach(i -> this.ticketRegistry.addTicket(new MockServiceTicket("T" + i, RegisteredServiceTestUtils.getService(),
                new MockTicketGrantingTicket("test"))));


        status = CacheStatus.class.cast(monitor.observe());
        stats = getServiceTicketStats(status);
        assertEquals(100, stats.getCapacity());
        assertEquals(95, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());

        // Exceed the capacity and force evictions which should report WARN status
        IntStream.range(95, 110).forEach(i -> {
            final MockServiceTicket st = new MockServiceTicket("T" + i, RegisteredServiceTestUtils.getService(),
                    new MockTicketGrantingTicket("test"));
            this.ticketRegistry.addTicket(st);
        });

        status = CacheStatus.class.cast(monitor.observe());
        stats = getServiceTicketStats(status);
        assertEquals(100, stats.getCapacity());
        assertEquals(100, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());
    }

    private CacheStatistics getServiceTicketStats(final CacheStatus status) {
        return Arrays.stream(status.getStatistics()).filter(c ->
                c.getName().equalsIgnoreCase(EhcacheTicketRegistryTicketCatalogConfiguration.SERVICE_TICKETS_CACHE))
                .findFirst()
                .get();
    }
}
