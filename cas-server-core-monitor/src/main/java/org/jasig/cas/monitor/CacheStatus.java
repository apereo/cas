package org.jasig.cas.monitor;

import com.google.common.collect.ImmutableList;

import javax.validation.constraints.NotNull;

/**
 * Describes meaningful health metrics on the status of a cache.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class CacheStatus extends Status {

    private final CacheStatistics[] statistics;

    /**
     * Creates a new instance describing cache status.
     *
     * @param code Status code.
     * @param description Optional status description.
     * @param statistics One or more sets of cache statistics.
     */
    public CacheStatus(final StatusCode code, final String description, @NotNull final CacheStatistics... statistics) {
        super(code, buildDescription(description, statistics));
        this.statistics = statistics;
    }


    /**
     * Creates a new instance when cache statistics are unavailable due to given exception.
     *
     * @param e Cause of unavailable statistics.
     */
    public CacheStatus(final Exception e) {
        super(StatusCode.ERROR,
                String.format("Error fetching cache status: %s::%s", e.getClass().getSimpleName(), e.getMessage()));
        this.statistics = null;
    }


    /**
     * Gets the current cache statistics.
     *
     * @return Cache statistics.
     */
    public CacheStatistics[] getStatistics() {
        return ImmutableList.copyOf(this.statistics).toArray(new CacheStatistics[this.statistics.length]);
    }


    /**
     * Builds the description string for the retrieved statistics.
     *
     * @param desc the desc
     * @param statistics the statistics
     * @return the string
     */
    private static String buildDescription(final String desc, final CacheStatistics... statistics) {
        if (statistics == null || statistics.length == 0) {
            return desc;
        }
        final StringBuilder sb = new StringBuilder();
        if (desc != null) {
            sb.append(desc);
            if (!desc.endsWith(".")) {
                sb.append('.');
            }
            sb.append(' ');
        }
        sb.append("Cache statistics: [");
        int i = 0;
        for (final CacheStatistics stats : statistics) {
            if (i++ > 0) {
                sb.append('|');
            }
            stats.toString(sb);
        }
        sb.append(']');
        return sb.toString();
    }
}
