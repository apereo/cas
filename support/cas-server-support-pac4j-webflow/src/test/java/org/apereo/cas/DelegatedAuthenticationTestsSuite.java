package org.apereo.cas;

import org.apereo.cas.support.pac4j.RefreshableDelegatedClientsTests;
import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationControllerTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderRedirectionStrategyTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientAuthenticationWebflowManagerTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientFinishLogoutActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientLogoutActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientRetryActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolverTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationSAMLConfigurationTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationPostProcessorTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataControllerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link DelegatedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DelegatedClientIdentityProviderRedirectionStrategyTests.class,
    RefreshableDelegatedClientsTests.class,
    DelegatedClientIdentityProviderConfigurationPostProcessorTests.class,
    DelegatedAuthenticationSAMLConfigurationTests.class,
    DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests.class,
    DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests.class,
    DelegatedClientAuthenticationActionTests.class,
    DelegatedClientIdentityProviderConfigurationProducerTests.class,
    DelegatedAuthenticationErrorViewResolverTests.class,
    DefaultDelegatedAuthenticationNavigationControllerTests.class,
    DelegatedAuthenticationClientLogoutActionTests.class,
    DelegatedAuthenticationClientFinishLogoutActionTests.class,
    DefaultDelegatedClientAuthenticationWebflowManagerTests.class,
    DefaultDelegatedClientIdentityProviderConfigurationProducerTests.class,
    DelegatedAuthenticationClientRetryActionTests.class,
    DelegatedAuthenticationWebflowConfigurerTests.class,
    DelegatedSaml2ClientMetadataControllerTests.class
})
@Suite
public class DelegatedAuthenticationTestsSuite {
}
