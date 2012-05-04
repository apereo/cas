/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link PoolingContextSourceMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/ldapContext-test.xml")
public class PoolingContextSourceMonitorTests {

    @Autowired
    private PoolingContextSource poolingContextSource;
    
    private PoolingContextSourceMonitor monitor;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() throws Exception {
        monitor = new PoolingContextSourceMonitor();
        monitor.setPoolingContextSource(poolingContextSource);
        monitor.setExecutor(executor);
    }

    @Test
    public void testObserveOK() throws Exception {
        monitor.setMaxWait(5000);
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void testObserveWarn() throws Exception {
        monitor.setMaxWait(5);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }
}
