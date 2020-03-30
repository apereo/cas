package org.apereo.cas;

import org.apereo.cas.monitor.CasJdbcMonitorConfigurationTests;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicatorTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    CasJdbcMonitorConfigurationTests.class,
    JdbcDataSourceHealthIndicatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}

