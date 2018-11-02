package org.apereo.cas;

import org.apereo.cas.web.security.authentication.LdapAuthenticationProviderRolesBasedTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllWebflowTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    WiringConfigurationTests.class,
    CasWebflowServerSessionContextConfigurationTests.class,
    CasWebflowClientSessionContextConfigurationTests.class,
    LdapAuthenticationProviderRolesBasedTests.class
})
public class AllWebflowTestsSuite {
}
