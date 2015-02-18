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

import javax.validation.constraints.NotNull;

import net.sf.ehcache.Cache;

/**
 * Monitors a {@link net.sf.ehcache.Cache} instance.
 * The accuracy of statistics is governed by the value of {@link Cache#getStatistics()}.
 *
 * <p>NOTE: computation of highly accurate statistics is expensive.</p>
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class EhCacheMonitor extends AbstractCacheMonitor {

    @NotNull
    private final Cache cache;

    /**
     * Instantiates a new EhCache monitor.
     *
     * @param cache the cache
     */
    public EhCacheMonitor(final Cache cache) {
        this.cache = cache;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        return new EhCacheStatistics[] {new EhCacheStatistics(cache)};
    }
}
