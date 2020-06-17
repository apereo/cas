package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationHandlerTests;
import org.apereo.cas.authentication.AuthenticationPostProcessorTests;
import org.apereo.cas.authentication.AuthenticationPreProcessorTests;
import org.apereo.cas.authentication.AuthenticationTransactionTests;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationExceptionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    UnauthorizedAuthenticationExceptionTests.class,
    AuthenticationHandlerTests.class,
    AuthenticationPreProcessorTests.class,
    AuthenticationPostProcessorTests.class,
    AuthenticationTransactionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllAuthenticationTestsSuite {
}
