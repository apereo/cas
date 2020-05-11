package org.apereo.cas;

import org.apereo.cas.gauth.GoogleAuthenticatorAuthenticationHandlerTests;
import org.apereo.cas.gauth.GoogleAuthenticatorServiceTests;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link GoogleAuthenticatorCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorAuthenticationHandlerTests.class,
    GoogleAuthenticatorServiceTests.class
})
@RunWith(JUnitPlatform.class)
public class GoogleAuthenticatorCoreTestsSuite {
}
