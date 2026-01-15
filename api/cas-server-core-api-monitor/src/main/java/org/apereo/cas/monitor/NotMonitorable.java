package org.apereo.cas.monitor;

import module java.base;

/**
 * For components that should not be monitored.
 *
 * @author Jerome LELEU
 * @since 7.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NotMonitorable {
}
