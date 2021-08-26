package org.apereo.cas;

import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandlerTests;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowActionTests;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurerTests;
import org.apereo.cas.config.AuthyAuthenticationMultifactorProviderBypassConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AuthyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuthyAuthenticationHandlerTests.class,
    AuthyAuthenticationMultifactorProviderBypassConfigurationTests.class,
    AuthyAuthenticationWebflowEventResolverTests.class,
    AuthyMultifactorWebflowConfigurerTests.class,
    AuthyAuthenticationRegistrationWebflowActionTests.class
})
@Suite
public class AuthyTestsSuite {
}
