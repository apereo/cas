package org.apereo.cas;

import org.apereo.cas.adaptors.jdbc.config.CasJdbcAuthenticationConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@SelectClasses(CasJdbcAuthenticationConfigurationTests.class)
@Suite
public class AllTestsSuite {
}
