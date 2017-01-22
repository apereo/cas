package org.apereo.cas.monitor;

import org.apereo.cas.AbstractMemcachedTests;
import org.apereo.cas.monitor.config.MemcachedMonitorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link MemcachedMonitorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, MemcachedMonitorConfiguration.class})
@ContextConfiguration(locations = "/monitor-test.xml")
public class MemcachedMonitorTests extends AbstractMemcachedTests {

    @Autowired
    @Qualifier("memcachedMonitor")
    private MemcachedMonitor monitor;


    @Test
    public void verifyMonitorRunning() {
        this.monitor.observe();
    }
}
