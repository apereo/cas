package org.apereo.cas.adaptors.authy;

import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowActionTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AuthyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuthyAuthenticationHandlerTests.class,
    AuthyAuthenticationRegistrationWebflowActionTests.class
})
public class AuthyTestsSuite {
}
