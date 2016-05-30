package org.apereo.cas.monitor;

import net.spy.memcached.MemcachedClientIF;
import org.apereo.cas.AbstractMemcachedTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * This is {@link MemcachedMonitorTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/monitor-test.xml")
public class MemcachedMonitorTests extends AbstractMemcachedTests {

    @Resource(name="memcachedMonitor")
    private MemcachedMonitor monitor;

    @Resource(name="memcachedClient")
    private MemcachedClientIF memcachedClient;


    @BeforeClass
    public static void beforeClass() throws IOException {
        bootstrap();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        shutdown();
    }

    @Test
    public void verifyMonitorRunning() {
        this.monitor.observe();
    }
}
