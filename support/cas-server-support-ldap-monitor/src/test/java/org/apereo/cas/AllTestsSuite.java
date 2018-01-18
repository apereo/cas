package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.monitor.PooledConnectionFactoryHealthIndicatorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(PooledConnectionFactoryHealthIndicatorTests.class)
@Slf4j
public class AllTestsSuite {
}
