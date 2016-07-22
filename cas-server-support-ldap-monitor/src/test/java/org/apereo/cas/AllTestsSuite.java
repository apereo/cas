package org.apereo.cas;

import org.apereo.cas.monitor.PooledConnectionFactoryMonitorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all LDAP tests.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(PooledConnectionFactoryMonitorTests.class)
public class AllTestsSuite {
}
