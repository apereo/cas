package org.apereo.cas;

import org.apereo.cas.pm.DefaultPasswordValidationServiceTests;
import org.apereo.cas.pm.PasswordManagementQueryTests;
import org.apereo.cas.pm.PasswordManagementServiceTests;
import org.apereo.cas.pm.history.AmnesiacPasswordHistoryServiceTests;
import org.apereo.cas.pm.history.GroovyPasswordHistoryServiceTests;
import org.apereo.cas.pm.history.InMemoryPasswordHistoryServiceTests;
import org.apereo.cas.pm.impl.GroovyResourcePasswordManagementServiceTests;
import org.apereo.cas.pm.impl.JsonResourcePasswordManagementServiceTests;
import org.apereo.cas.pm.impl.NoOpPasswordManagementServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllPasswordManagementServiceTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    JsonResourcePasswordManagementServiceTests.class,
    PasswordManagementServiceTests.class,
    PasswordManagementQueryTests.class,
    AmnesiacPasswordHistoryServiceTests.class,
    NoOpPasswordManagementServiceTests.class,
    GroovyResourcePasswordManagementServiceTests.class,
    GroovyPasswordHistoryServiceTests.class,
    DefaultPasswordValidationServiceTests.class,
    InMemoryPasswordHistoryServiceTests.class
})
@Suite
public class AllPasswordManagementServiceTestsSuite {
}
