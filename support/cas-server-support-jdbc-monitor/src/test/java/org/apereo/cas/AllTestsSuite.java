package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicatorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all jdbc test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses(JdbcDataSourceHealthIndicatorTests.class)
@Slf4j
public class AllTestsSuite {
}
