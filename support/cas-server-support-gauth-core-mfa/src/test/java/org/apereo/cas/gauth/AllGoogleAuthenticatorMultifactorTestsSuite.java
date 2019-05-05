package org.apereo.cas.gauth;

import org.apereo.cas.gauth.rest.GoogleAuthenticatorRestHttpRequestCredentialFactoryTests;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllGoogleAuthenticatorMultifactorTestsSuite {
}
