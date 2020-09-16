package org.apereo.cas;

import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationControllerTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationClientLogoutActionTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolverTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationFunctionTests;
import org.apereo.cas.web.saml2.Saml2ClientMetadataControllerTests;

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
    DelegatedClientIdentityProviderConfigurationFunctionTests.class,
    DelegatedAuthenticationErrorViewResolverTests.class,
    DefaultDelegatedAuthenticationNavigationControllerTests.class,
    DelegatedAuthenticationClientLogoutActionTests.class,
    Saml2ClientMetadataControllerTests.class
})
@RunWith(JUnitPlatform.class)
public class DelegatedAuthenticationTestsSuite {
}
