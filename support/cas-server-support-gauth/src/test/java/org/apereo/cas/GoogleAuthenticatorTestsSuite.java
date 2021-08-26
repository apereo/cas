package org.apereo.cas;

import org.apereo.cas.gauth.config.GoogleAuthenticatorConfigurationTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorOneTimeTokenCredentialValidatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredentialRepositoryEndpointTests;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.JsonGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.RestGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurerTests;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorPrepareLoginActionTests;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorSaveRegistrationActionTests;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorValidateSelectedRegistrationActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link GoogleAuthenticatorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    GoogleAuthenticatorValidateSelectedRegistrationActionTests.class,
    GoogleAuthenticatorConfigurationTests.class,
    GoogleAuthenticatorOneTimeTokenCredentialValidatorTests.class,
    GoogleAuthenticatorSaveRegistrationActionTests.class,
    GoogleAuthenticatorPrepareLoginActionTests.class,
    GoogleAuthenticatorMultifactorWebflowConfigurerTests.class,
    GoogleAuthenticatorTokenCredentialRepositoryEndpointTests.class,
    RestGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    JsonGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
@Suite
public class GoogleAuthenticatorTestsSuite {
}
