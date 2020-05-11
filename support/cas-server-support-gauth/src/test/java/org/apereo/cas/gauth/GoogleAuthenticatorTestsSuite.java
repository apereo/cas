package org.apereo.cas.gauth;

import org.apereo.cas.gauth.config.GoogleAuthenticatorConfigurationTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredentialRepositoryEndpointTests;
import org.apereo.cas.gauth.credential.JsonGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.RestGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link GoogleAuthenticatorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    GoogleAuthenticatorConfigurationTests.class,
    GoogleAuthenticatorMultifactorWebflowConfigurerTests.class,
    GoogleAuthenticatorTokenCredentialRepositoryEndpointTests.class,
    RestGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    JsonGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class GoogleAuthenticatorTestsSuite {
}
