
package org.apereo.cas;

import org.apereo.cas.pm.ADPasswordManagementServiceTests;
import org.apereo.cas.pm.LdapPasswordManagementServiceTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    ADPasswordManagementServiceTests.class,
    LdapPasswordManagementServiceTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
