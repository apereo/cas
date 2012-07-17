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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;

import net.spy.memcached.MemcachedClient;

/**
 * Monitors the memcached hosts known to an instance of {@link net.spy.memcached.MemcachedClient}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class MemcachedMonitor extends AbstractCacheMonitor {

    @NotNull
    private final MemcachedClient memcachedClient;


    /**
     * Creates a new monitor that observes the given memcached client.
     *
     * @param client Memcached client.
     */
    public MemcachedMonitor(final MemcachedClient client) {
        this.memcachedClient = client;
    }


    /**
     * Supersede the default cache status algorithm by considering unavailable memcached nodes above cache statistics.
     * If all nodes are unavailable, raise an error; if one or more nodes are unavailable, raise a warning; otherwise
     * delegate to examination of cache statistics.
     *
     * @return Cache status descriptor.
     */
    public CacheStatus observe() {
        if (memcachedClient.getAvailableServers().size() == 0) {
            return new CacheStatus(new RuntimeException("No memcached servers available."));
        }
        final Collection<SocketAddress> unavailableList = memcachedClient.getUnavailableServers();
        final CacheStatus status;
        if (unavailableList.size() > 0) {
            final String description = "One or more memcached servers is unavailable: " + unavailableList;
            status = new CacheStatus(StatusCode.WARN, description, getStatistics());
        } else {
            status = super.observe();
        }
        return status;
    }


    /**
     * Get cache statistics for all memcached hosts known to {@link MemcachedClient}.
     *
     * @return Statistics for all available hosts.
     */
    protected CacheStatistics[] getStatistics() {
        long evictions;
        long size;
        long capacity;
        String name;
        int i = 0;
        final Map<SocketAddress, Map<String, String>> allStats = memcachedClient.getStats();
        final SimpleCacheStatistics[] statistics = new SimpleCacheStatistics[allStats.size()];
        for (Map.Entry<SocketAddress, Map<String, String>> entry : allStats.entrySet()) {
            size = Long.parseLong(entry.getValue().get("bytes"));
            capacity = Long.parseLong(entry.getValue().get("limit_maxbytes"));
            evictions = Long.parseLong(entry.getValue().get("evictions"));
            statistics[i] = new SimpleCacheStatistics(size, capacity, evictions);
            if (entry.getKey() instanceof InetSocketAddress) {
                name = ((InetSocketAddress) entry.getKey()).getHostName();
            } else {
                name = entry.getKey().toString();
            }
            statistics[i].setName(name);
            i++;
        }
        return statistics;
    }
}
