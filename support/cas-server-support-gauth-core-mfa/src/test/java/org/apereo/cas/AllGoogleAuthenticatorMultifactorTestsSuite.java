package org.apereo.cas;

import org.apereo.cas.gauth.GoogleAuthenticatorMultifactorAuthenticationProviderTests;
import org.apereo.cas.gauth.rest.GoogleAuthenticatorRestHttpRequestCredentialFactoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllGoogleAuthenticatorMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    GoogleAuthenticatorMultifactorAuthenticationProviderTests.class,
    GoogleAuthenticatorRestHttpRequestCredentialFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllGoogleAuthenticatorMultifactorTestsSuite {
}
