package org.apereo.cas.util.thread;

/**
 * Describes a resource that supports purging auditing, statistics, or
 * error data that meets arbitrary criteria.
 *
 * @author Marvin S. Addison
 * @since 1.0
 */
public interface Cleanable {
    /**
     * Purges records meeting arbitrary criteria defined by implementers.
     */
    default void clean() {}
}
