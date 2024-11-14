package org.apereo.cas.sentry;

import org.apereo.cas.monitor.NotMonitorable;

/**
 * This is {@link Reporter}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@NotMonitorable
public interface Reporter {
    /**
     * Report object.
     *
     * @return the object
     */
    Object report();
}
