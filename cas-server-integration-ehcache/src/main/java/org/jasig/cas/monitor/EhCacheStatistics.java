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
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.apache.commons.lang3.StringUtils;
import java.util.Formatter;

/**
 * Ehcache statistics wrapper.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class EhCacheStatistics implements CacheStatistics {

    private static final double TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE = 1048510.0;
    private static final int PERCENTAGE_VALUE = 100;
    private final Cache cache;

    // Flag to determine whether size units are in bytes or simple object counts
    private boolean useBytes;

    private long diskSize;

    private long heapSize;

    // Off heap size is always in units of bytes
    private long offHeapSize;

    /**
     * Creates a new instance that delegates statistics inquiries to the given {@link Cache} instance.
     *
     * @param cache Cache instance for which to gather statistics.
     */
    public EhCacheStatistics(final Cache cache) {
        this.cache = cache;
        if (cache.getCacheConfiguration().getMaxBytesLocalDisk() > 0) {
            useBytes = true;
        }
    }

    /**
     * Gets the size of heap consumed by items stored in the cache.
     *
     * @return Memory size.
     */
    @Override
    public long getSize() {
        final StatisticsGateway statistics = cache.getStatistics();
        // Store component sizes on each call to avoid recalculating
        // sizes in other methods that need them
        if (useBytes) {
            diskSize = statistics.getLocalDiskSizeInBytes();
            heapSize = statistics.getLocalHeapSizeInBytes();
        } else {
            diskSize = cache.getDiskStoreSize();
            heapSize = cache.getMemoryStoreSize();
        }
        offHeapSize = statistics.getLocalOffHeapSizeInBytes();
        return heapSize;
    }

    /**
     * Gets the heap memory capacity of the cache.
     *
     * @return Heap memory capacity.
     */
    @Override
    public long getCapacity() {
        final CacheConfiguration config = cache.getCacheConfiguration();
        if (useBytes) {
            return config.getMaxBytesLocalDisk();
        }
        return config.getMaxElementsOnDisk();
    }

    @Override
    public long getEvictions() {
        return cache.getStatistics().cacheEvictedCount();
    }

    @Override
    public int getPercentFree() {
        final long capacity = getCapacity();
        if (capacity == 0) {
            return 0;
        }
        return (int) ((capacity - getSize()) * PERCENTAGE_VALUE / capacity);
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public void toString(final StringBuilder builder) {
        final String name = this.getName();
        if (StringUtils.isNotBlank(name)) {
            builder.append(name).append(':');
        }
        final int free = getPercentFree();
        final Formatter formatter = new Formatter(builder);
        if (useBytes) {
            formatter.format("%.2f", heapSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
            builder.append("MB heap, ");
            formatter.format("%.2f", diskSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
            builder.append("MB disk, ");
        } else {
            builder.append(heapSize).append(" items in heap, ");
            builder.append(diskSize).append(" items on disk, ");
        }
        formatter.format("%.2f", offHeapSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
        builder.append("MB off-heap, ");
        builder.append(free).append("% free, ");
        builder.append(getEvictions()).append(" evictions");
        formatter.close();
    }
}
