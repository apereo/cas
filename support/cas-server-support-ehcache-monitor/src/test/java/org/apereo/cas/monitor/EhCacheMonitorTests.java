package org.apereo.cas.monitor;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apereo.cas.monitor.config.EhCacheMonitorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for {@link EhCacheMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EhCacheMonitorConfiguration.class)
@ContextConfiguration(locations = {"classpath:ehcacheMonitor-test.xml"})
public class EhCacheMonitorTests {

    @Autowired
    @Qualifier("ehcacheTicketsCache")
    private Cache cache;

    @Autowired
    @Qualifier("ehcacheMonitor")
    private Monitor monitor;

    @Test
    public void verifyObserve() throws Exception {
        CacheStatus status = CacheStatus.class.cast(monitor.observe());
        CacheStatistics stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(0, stats.getSize());
        assertEquals(StatusCode.OK, status.getCode());

        // Fill cache 95% full, which is above 10% free WARN threshold
        IntStream.range(0, 95).forEach(i -> cache.put(new Element("key" + i, "value" + i)));

        status = CacheStatus.class.cast(monitor.observe());
        stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(95, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());

        // Exceed the capacity and force evictions which should report WARN status
        IntStream.range(95, 110).forEach(i -> cache.put(new Element("key" + i, "value" + i)));

        status = CacheStatus.class.cast(monitor.observe());
        stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(100, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());
    }
}
