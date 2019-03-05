package org.apereo.cas;

import org.apereo.cas.web.security.authentication.MonitorEndpointLdapAuthenticationProviderGroupsBasedTests;
import org.apereo.cas.web.security.authentication.MonitorEndpointLdapAuthenticationProviderRolesBasedTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllWebflowTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    WiringConfigurationTests.class,
    CasWebflowServerSessionContextConfigurationTests.class,
    CasWebflowClientSessionContextConfigurationTests.class,
    MonitorEndpointLdapAuthenticationProviderRolesBasedTests.class,
    MonitorEndpointLdapAuthenticationProviderGroupsBasedTests.class
})
public class AllWebflowTestsSuite {
}
