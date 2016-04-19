package org.jasig.cas.monitor;

import net.spy.memcached.MemcachedClientIF;
import org.jasig.cas.AbstractMemcachedTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @Autowired
    @Qualifier("memcachedMonitor")
    private MemcachedMonitor monitor;

    @Autowired
    @Qualifier("memcachedClient")
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
