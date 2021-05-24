package org.apereo.cas;

import org.apereo.cas.gauth.GoogleAuthenticatorAuthenticationHandlerTests;
import org.apereo.cas.gauth.GoogleAuthenticatorServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link GoogleAuthenticatorCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    GoogleAuthenticatorAuthenticationHandlerTests.class,
    GoogleAuthenticatorServiceTests.class
})
@Suite
public class GoogleAuthenticatorCoreTestsSuite {
}
