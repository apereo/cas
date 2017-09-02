package org.apereo.cas.monitor;

/**
 * Describes the simplest set of cache statistics that are meaningful for health monitoring.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public interface CacheStatistics {

    /**
     * Gets the current size of the cache in a unit specific to the cache being monitored (e.g. bytes, items, etc).
     *
     * @return Current cache size.
     */
    default long getSize() {
        return Status.UNKNOWN.getCode().value();
    }

    /**
     * Gets the current capacity of the cache in a unit specific to the cache being monitored (e.g. bytes, items, etc).
     *
     * @return Current cache capacity.
     */
    default long getCapacity() {
        return Status.UNKNOWN.getCode().value();
    }

    /**
     * Gets the number of items evicted from the cache in order to make space for new items.
     *
     * @return Eviction count.
     */
    default long getEvictions() {
        return Status.UNKNOWN.getCode().value();
    }

    /**
     * Gets the percent free capacity remaining in the cache.
     *
     * @return Percent of space/capacity free.
     */
    default int getPercentFree() {
        return Status.UNKNOWN.getCode().value();
    }

    /**
     * Gets a descriptive name of the cache instance for which statistics apply.
     *
     * @return Name of cache instance/host to which statistics apply.
     */
    String getName();

    /**
     * Writes a string representation of cache statistics to the given string builder.
     *
     * @param builder String builder to which string representation is appended.
     */
    void toString(StringBuilder builder);

}
