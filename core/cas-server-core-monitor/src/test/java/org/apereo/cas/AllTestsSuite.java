
package org.apereo.cas;

import org.apereo.cas.monitor.AbstractCacheHealthIndicatorTests;
import org.apereo.cas.monitor.CacheHealthIndicatorTests;
import org.apereo.cas.monitor.CasCoreMonitorConfigurationTests;
import org.apereo.cas.monitor.MemoryHealthIndicatorTests;
import org.apereo.cas.monitor.PoolHealthIndicatorTests;
import org.apereo.cas.monitor.SessionHealthIndicatorTests;
import org.apereo.cas.monitor.TicketRegistryHealthIndicatorTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CasCoreMonitorConfigurationTests.class,
    MemoryHealthIndicatorTests.class,
    PoolHealthIndicatorTests.class,
    AbstractCacheHealthIndicatorTests.class,
    TicketRegistryHealthIndicatorTests.class,
    SessionHealthIndicatorTests.class,
    CacheHealthIndicatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
