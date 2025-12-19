package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasMemcachedMonitorAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MemcachedHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 * @deprecated Since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasMemcachedMonitorAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
}, properties = {
    "cas.monitor.memcached.servers=localhost:11212",
    "cas.monitor.memcached.failure-mode=Redistribute",
    "cas.monitor.memcached.locator-type=ARRAY_MOD",
    "cas.monitor.memcached.hash-algorithm=FNV1A_64_HASH"
})
@Tag("Memcached")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 11211)
@Deprecated(since = "7.0.0")
class MemcachedHealthIndicatorTests {
    @Autowired
    @Qualifier("memcachedHealthIndicator")
    private HealthIndicator monitor;

    @Test
    void verifyMonitorNotRunning() {
        val health = monitor.health();
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
    }

    @Test
    void verifyUnavailableServers() throws Throwable {
        val memcached = mock(MemcachedClientIF.class);
        when(memcached.getUnavailableServers()).thenReturn(List.of(new InetSocketAddress(1234)));
        when(memcached.getAvailableServers()).thenReturn(List.of(new InetSocketAddress(11212)));
        val client = mock(ObjectPool.class);
        when(client.borrowObject()).thenReturn(memcached);
        val indicator = new MemcachedHealthIndicator(client, 1, 1);
        val health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void verifyMonitorError() throws Throwable {
        val memcached = mock(MemcachedClientIF.class);
        when(memcached.getUnavailableServers()).thenThrow(new RuntimeException("error"));
        when(memcached.getAvailableServers()).thenThrow(new RuntimeException("error"));
        val client = mock(ObjectPool.class);
        when(client.borrowObject()).thenReturn(memcached);
        val indicator = new MemcachedHealthIndicator(client, 1, 1);
        val health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void verifyMonitorSuccess() throws Throwable {
        val memcached = mock(MemcachedClientIF.class);
        when(memcached.getUnavailableServers()).thenReturn(List.of());
        val socket = new InetSocketAddress("localhost", 11212);
        when(memcached.getAvailableServers()).thenReturn(List.of(socket));

        val details = new HashMap<String, String>();
        details.put("bytes", "1000");
        details.put("limit_maxbytes", "500");
        details.put("evictions", "10");

        when(memcached.getStats()).thenReturn(Map.of(socket, details));
        val client = mock(ObjectPool.class);
        when(client.borrowObject()).thenReturn(memcached);
        val indicator = new MemcachedHealthIndicator(client, 1, 1);
        val health = indicator.health();
        assertEquals(new Status("WARN"), health.getStatus());
    }
}
