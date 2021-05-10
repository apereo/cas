package org.apereo.cas;

import org.apereo.cas.configuration.support.DataSourceProxyTests;
import org.apereo.cas.configuration.support.DefaultCloseableDataSourceTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SelectClasses({
    DataSourceProxyTests.class,
    DefaultCloseableDataSourceTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
