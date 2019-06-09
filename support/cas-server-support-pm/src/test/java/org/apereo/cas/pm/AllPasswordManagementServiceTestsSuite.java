package org.apereo.cas.pm;

import org.apereo.cas.pm.history.GroovyPasswordHistoryServiceTests;
import org.apereo.cas.pm.history.InMemoryPasswordHistoryServiceTests;
import org.apereo.cas.pm.impl.GroovyResourcePasswordManagementServiceTests;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementServiceTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllPasswordManagementServiceTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    JsonResourcePasswordManagementServiceTests.class,
    GroovyResourcePasswordManagementServiceTests.class,
    GroovyPasswordHistoryServiceTests.class,
    DefaultPasswordValidationServiceTests.class,
    InMemoryPasswordHistoryServiceTests.class
})
@RunWith(JUnitPlatform.class)
public class AllPasswordManagementServiceTestsSuite {
}
