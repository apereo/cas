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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import net.spy.memcached.MemcachedClientIF;

/**
 * Monitors the memcached hosts known to an instance of {@link net.spy.memcached.MemcachedClientIF}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class MemcachedMonitor extends AbstractCacheMonitor {

    @NotNull
    private final MemcachedClientIF memcachedClient;


    /**
     * Creates a new monitor that observes the given memcached client.
     *
     * @param client Memcached client.
     */
    public MemcachedMonitor(final MemcachedClientIF client) {
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
            return new CacheStatus(StatusCode.ERROR, "No memcached servers available.");
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
     * Get cache statistics for all memcached hosts known to {@link MemcachedClientIF}.
     *
     * @return Statistics for all available hosts.
     */
    protected CacheStatistics[] getStatistics() {


        final Map<SocketAddress, Map<String, String>> allStats = memcachedClient.getStats();
        final List<CacheStatistics> statsList = new ArrayList<>();
        for (final Map.Entry<SocketAddress, Map<String, String>> entry : allStats.entrySet()) {
            final SocketAddress key = entry.getKey();
            final Map<String, String> statsMap = entry.getValue();

            if (statsMap.size() > 0) {
                final long size = Long.parseLong(statsMap.get("bytes"));
                final long capacity = Long.parseLong(statsMap.get("limit_maxbytes"));
                final long evictions = Long.parseLong(statsMap.get("evictions"));

                final String name;
                if (key instanceof InetSocketAddress) {
                    name = ((InetSocketAddress) key).getHostName();
                } else {
                    name = key.toString();
                }
                statsList.add(new SimpleCacheStatistics(size, capacity, evictions, name));
            }
        }
        return statsList.toArray(new CacheStatistics[statsList.size()]);
    }
}
