package org.apereo.cas;

import org.apereo.cas.services.YamlServiceRegistryConfigurationTests;
import org.apereo.cas.services.YamlServiceRegistryTests;

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
    YamlServiceRegistryConfigurationTests.class,
    YamlServiceRegistryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllYamlTestsSuite {
}
