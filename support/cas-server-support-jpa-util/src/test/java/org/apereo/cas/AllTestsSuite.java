package org.apereo.cas;

import org.apereo.cas.configuration.support.DataSourceProxyTests;
import org.apereo.cas.configuration.support.DefaultCloseableDataSourceTests;
import org.apereo.cas.configuration.support.JpaBeansTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SelectClasses({
    JpaBeansTests.class,
    DataSourceProxyTests.class,
    DefaultCloseableDataSourceTests.class
})
@Suite
public class AllTestsSuite {
}
