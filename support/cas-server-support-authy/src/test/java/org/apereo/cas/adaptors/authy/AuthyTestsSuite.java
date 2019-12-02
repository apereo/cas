package org.apereo.cas.adaptors.authy;

import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowActionTests;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AuthyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuthyAuthenticationHandlerTests.class,
    AuthyMultifactorWebflowConfigurerTests.class,
    AuthyAuthenticationRegistrationWebflowActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AuthyTestsSuite {
}
