package org.apereo.cas;

import org.apereo.cas.monitor.PooledConnectionFactoryHealthIndicatorTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SelectClasses(PooledConnectionFactoryHealthIndicatorTests.class)
public class AllTestsSuite {
}
