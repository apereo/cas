/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import net.spy.memcached.MemcachedClientIF;
import org.jasig.cas.AbstractMemcacheTests;
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
public class MemcachedMonitorTests extends AbstractMemcacheTests {

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
