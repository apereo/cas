
package org.apereo.cas;

import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandlerTests;
import org.apereo.cas.authentication.support.GroovyPasswordPolicyHandlingStrategyTests;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandlerTests;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategyTests;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGeneratorTests;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGeneratorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    LdapUtilsTests.class,
    LdapUserGroupsToRolesAuthorizationGeneratorTests.class,
    LdapUserAttributesToRolesAuthorizationGeneratorTests.class,
    DefaultLdapAccountStateHandlerTests.class,
    GroovyPasswordPolicyHandlingStrategyTests.class,
    OptionalWarningLdapAccountStateHandlerTests.class,
    RejectResultCodeLdapPasswordPolicyHandlingStrategyTests.class
})
@Suite
public class AllTestsSuite {
}
