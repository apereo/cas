package org.apereo.cas.monitor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Formatter;
import lombok.Getter;

/**
 * Simple implementation of cache statistics.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Slf4j
@Getter
@AllArgsConstructor
public class SimpleCacheStatistics implements CacheStatistics {

    private static final double BYTES_PER_MB = 1048510.0;

    private static final int PERCENTAGE_VALUE = 100;

    private final long size;

    private final long capacity;

    private final long evictions;

    private String name;

    public SimpleCacheStatistics(final long size, final long capacity, final long evictions) {
        this(size, capacity, evictions, "N/A");
    }
    

    @Override
    public int getPercentFree() {
        if (this.capacity == 0) {
            return 0;
        }
        return (int) ((this.capacity - this.size) * PERCENTAGE_VALUE / this.capacity);
    }

    @Override
    public void toString(final StringBuilder builder) {
        if (this.name != null) {
            builder.append(this.name).append(':');
        }
        try (Formatter formatter = new Formatter(builder)) {
            formatter.format("%.2f", this.size / BYTES_PER_MB);
            builder.append("MB used, ");
            builder.append(getPercentFree()).append("% free, ");
            builder.append(this.evictions).append(" evictions");
        }
    }
}
