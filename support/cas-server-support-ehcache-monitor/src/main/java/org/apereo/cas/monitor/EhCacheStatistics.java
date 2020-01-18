package org.apereo.cas.monitor;

import lombok.val;
import net.sf.ehcache.Cache;
import org.apache.commons.lang3.StringUtils;

import java.util.Formatter;

/**
 * Ehcache statistics wrapper.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Deprecated(since = "6.2.0")
public class EhCacheStatistics implements CacheStatistics {

    private static final double TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE = 1048510.0;
    private static final int PERCENTAGE_VALUE = 100;

    private final Cache cache;

    private final boolean useBytes;

    private long diskSize;

    private long heapSize;

    /**
     * Off heap size is always in units of bytes.
     */
    private long offHeapSize;

    public EhCacheStatistics(final Cache cache) {
        this.cache = cache;
        this.useBytes = cache.getCacheConfiguration().getMaxBytesLocalDisk() > 0;
    }

    @Override
    public long getSize() {
        val statistics = this.cache.getStatistics();
        if (this.useBytes) {
            this.diskSize = statistics.getLocalDiskSizeInBytes();
            this.heapSize = statistics.getLocalHeapSizeInBytes();
        } else {
            this.diskSize = statistics.getLocalDiskSize();
            this.heapSize = statistics.getLocalHeapSize();
        }
        this.offHeapSize = statistics.getLocalOffHeapSizeInBytes();
        return this.heapSize;
    }

    @Override
    public long getCapacity() {
        val config = this.cache.getCacheConfiguration();
        if (this.useBytes) {
            return config.getMaxBytesLocalDisk();
        }
        return config.getMaxEntriesLocalDisk();
    }

    @Override
    public long getEvictions() {
        return this.cache.getStatistics().cacheEvictedCount();
    }

    @Override
    public long getPercentFree() {
        val capacity = getCapacity();
        if (capacity == 0) {
            return 0;
        }
        return (int) ((capacity - getSize()) * PERCENTAGE_VALUE / capacity);
    }

    @Override
    public String getName() {
        return this.cache.getName();
    }

    @Override
    public String toString(final StringBuilder builder) {
        val name = this.getName();
        if (StringUtils.isNotBlank(name)) {
            builder.append(name).append(':');
        }
        try (val formatter = new Formatter(builder)) {
            if (this.useBytes) {
                formatter.format("%.2f MB heap, ", this.heapSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
                formatter.format("%.2f MB disk, ", this.diskSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
            } else {
                builder.append(this.heapSize).append(" items in heap, ");
                builder.append(this.diskSize).append(" items on disk, ");
            }
            formatter.format("%.2f MB off-heap, ", this.offHeapSize / TOTAL_NUMBER_BYTES_IN_ONE_MEGABYTE);
            builder.append(getPercentFree()).append(" perfect free, ");
            builder.append(getEvictions()).append(" evictions");
        }
        return builder.toString();
    }
}
