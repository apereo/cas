package org.apereo.cas;

import org.apereo.cas.monitor.JdbcDataSourceHealthIndicatorTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all jdbc test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@SelectClasses(JdbcDataSourceHealthIndicatorTests.class)
public class AllTestsSuite {
}
