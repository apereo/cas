package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link CredentialRepositoryTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    JsonGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    RestGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
public class CredentialRepositoryTestSuite {
}
