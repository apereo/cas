package org.apereo.cas;

import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationControllerTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientAuthenticationWebflowManagerTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientFinishLogoutActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientLogoutActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientRetryActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolverTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataControllerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link DelegatedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
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
@RunWith(JUnitPlatform.class)
public class DelegatedAuthenticationTestsSuite {
}
