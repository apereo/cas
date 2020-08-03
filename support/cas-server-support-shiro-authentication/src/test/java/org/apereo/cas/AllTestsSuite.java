package org.apereo.cas;

import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandlerTests;
import org.apereo.cas.config.ShiroAuthenticationConfigurationTests;

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
    ShiroAuthenticationHandlerTests.class,
    ShiroAuthenticationConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
