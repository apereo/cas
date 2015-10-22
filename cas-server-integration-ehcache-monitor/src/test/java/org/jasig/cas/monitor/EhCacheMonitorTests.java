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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link EhCacheMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/ehcacheMonitor-test.xml")
public class EhCacheMonitorTests {

    @Autowired
    private Cache cache;

    @Autowired
    private EhCacheMonitor monitor;

    @Test
    public void verifyObserve() throws Exception {
        CacheStatus status = monitor.observe();
        CacheStatistics stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(0, stats.getSize());
        assertEquals(StatusCode.OK, status.getCode());

        // Fill cache 95% full, which is above 10% free WARN threshold
        for (int i = 0; i < 95; i++) {
            cache.put(new Element("key" + i, "value" + i));
        }
        status = monitor.observe();
        stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(95, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());

        // Exceed the capacity and force evictions which should report WARN status
        for (int i = 95; i < 110; i++) {
            cache.put(new Element("key" + i, "value" + i));
        }
        status = monitor.observe();
        stats = status.getStatistics()[0];
        assertEquals(100, stats.getCapacity());
        assertEquals(100, stats.getSize());
        assertEquals(StatusCode.WARN, status.getCode());
    }
}
