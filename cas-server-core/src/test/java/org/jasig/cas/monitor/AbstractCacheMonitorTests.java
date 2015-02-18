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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheMonitor}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class AbstractCacheMonitorTests {
    @Test
    public void verifyObserveOk() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 0));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveError() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 1));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }


    @Test
    public void verifyObserveError2() throws Exception {
        // When cache has exceeded both thresholds, should report ERROR status
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 1));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }
}
