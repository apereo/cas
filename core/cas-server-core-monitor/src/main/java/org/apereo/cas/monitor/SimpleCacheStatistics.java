package org.apereo.cas.monitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.Formatter;

/**
 * Simple implementation of cache statistics.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Getter
@AllArgsConstructor
public class SimpleCacheStatistics implements CacheStatistics {

    private static final double BYTES_PER_MB = 1048510.0;

    private static final int PERCENTAGE_VALUE = 100;

    private final long size;

    private final long capacity;

    private final long evictions;

    private final String name;

    public SimpleCacheStatistics(final long size, final long capacity, final long evictions) {
        this(size, capacity, evictions, "N/A");
    }


    @Override
    public long getPercentFree() {
        if (this.capacity == 0) {
            return 0;
        }
        return (this.capacity - this.size) * PERCENTAGE_VALUE / this.capacity;
    }

    @Override
    public String toString(final StringBuilder builder) {
        if (this.name != null) {
            builder.append(this.name).append(':');
        }
        try (val formatter = new Formatter(builder)) {
            formatter.format("%.2f", this.size / BYTES_PER_MB);
        }
        builder.append("MB used, ");
        builder.append(getPercentFree()).append(" percent free, ");
        builder.append(this.evictions).append(" evictions");
        return builder.toString();
    }
}
