
package org.apereo.cas;

import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandlerTests;
import org.apereo.cas.authentication.support.GroovyPasswordPolicyHandlingStrategyTests;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandlerTests;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    LdapUtilsTests.class,
    DefaultLdapAccountStateHandlerTests.class,
    GroovyPasswordPolicyHandlingStrategyTests.class,
    OptionalWarningLdapAccountStateHandlerTests.class,
    RejectResultCodeLdapPasswordPolicyHandlingStrategyTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
