package org.apereo.cas.monitor;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.monitor.config.EhCacheMonitorConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link EhCacheHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 * @deprecated Since 6.2
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    EhcacheTicketRegistryConfiguration.class,
    EhcacheTicketRegistryTicketCatalogConfiguration.class,
    EhCacheMonitorConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.ticket.registry.ehcache.maxElementsOnDisk=100",
    "cas.ticket.registry.ehcache.maxElementsInMemory=100",
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.caches.enabled=true"
})
@Tag("Ehcache")
@Deprecated(since = "6.2.0")
public class EhCacheHealthIndicatorTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ehcacheHealthIndicator")
    private HealthIndicator monitor;

    @Test
    public void verifyObserve() {
        var status = monitor.health();
        assertEquals(Status.UP, status.getStatus());

        /*
         * Fill cache 95% full, which is
         * above 10% free WARN threshold
         */
        IntStream.range(0, 95)
            .forEach(i -> this.ticketRegistry.addTicket(
                new MockServiceTicket("ST-" + i, RegisteredServiceTestUtils.getService(),
                new MockTicketGrantingTicket("test"))));

        status = monitor.health();
        assertEquals(Status.OUT_OF_SERVICE, status.getStatus());

        /*
         * Exceed the capacity and force evictions
         * which should report WARN status
         */
        IntStream.range(95, 110).forEach(i -> {
            val st = new MockServiceTicket("ST-" + i, RegisteredServiceTestUtils.getService(),
                new MockTicketGrantingTicket("test"));
            this.ticketRegistry.addTicket(st);
        });

        status = monitor.health();
        assertEquals("WARN", status.getStatus().getCode());
    }
}
