package org.apereo.cas;

import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepositoryTests;
import org.apereo.cas.gauth.token.MariaDbGoogleAuthenticatorJpaTokenRepositoryTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link GoogleAuthenticatorJpaTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    MariaDbGoogleAuthenticatorJpaTokenRepositoryTests.class,
    MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorJpaTokenRepositoryTests.class
})
public class GoogleAuthenticatorJpaTestSuite {
}
