package org.apereo.cas;

import org.apereo.cas.monitor.CasJdbcMonitorConfigurationTests;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class AllTestsSuite {
}

