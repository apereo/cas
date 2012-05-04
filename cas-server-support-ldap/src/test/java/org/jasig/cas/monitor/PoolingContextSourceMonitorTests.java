/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
