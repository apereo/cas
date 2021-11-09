package org.apereo.cas;

import org.apereo.cas.config.WiringConfigurationTests;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapterDenyTests;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapterTests;
import org.apereo.cas.web.security.CasWebSecurityExpressionHandlerTests;
import org.apereo.cas.web.security.CasWebSecurityExpressionRootTests;
import org.apereo.cas.web.security.authentication.EndpointLdapAuthenticationProviderDefaultRolesTests;
import org.apereo.cas.web.security.authentication.EndpointLdapAuthenticationProviderGroupsBasedTests;
import org.apereo.cas.web.security.authentication.EndpointLdapAuthenticationProviderRolesBasedTests;
import org.apereo.cas.web.security.flow.PopulateSpringSecurityContextActionTests;
import org.apereo.cas.webflow.CasWebflowClientSessionContextConfigurationTests;
import org.apereo.cas.webflow.CasWebflowServerSessionContextConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllWebappTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    EndpointLdapAuthenticationProviderDefaultRolesTests.class,
    WiringConfigurationTests.class,
    PopulateSpringSecurityContextActionTests.class,
    CasWebSecurityConfigurerAdapterDenyTests.class,
    CasWebSecurityExpressionRootTests.class,
    CasWebSecurityExpressionHandlerTests.class,
    CasWebflowServerSessionContextConfigurationTests.class,
    CasWebSecurityConfigurerAdapterTests.class,
    CasWebflowClientSessionContextConfigurationTests.class,
    EndpointLdapAuthenticationProviderRolesBasedTests.class,
    EndpointLdapAuthenticationProviderGroupsBasedTests.class
})
@Suite
public class AllWebappTestsSuite {
}
