package org.apereo.cas;

import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandlerTests;
import org.apereo.cas.config.ShiroAuthenticationConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all LDAP tests.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 * @deprecated This component is deprecated as of 6.6.0 and is scheduled to be removed.
 */
@SelectClasses({
    ShiroAuthenticationHandlerTests.class,
    ShiroAuthenticationConfigurationTests.class
})
@Suite
@Deprecated(since = "6.6.0")
public class AllTestsSuite {
}
