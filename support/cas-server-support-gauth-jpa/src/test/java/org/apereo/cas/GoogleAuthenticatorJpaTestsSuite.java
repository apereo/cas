package org.apereo.cas;

import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepositoryTests;
import org.apereo.cas.gauth.token.MariaDbGoogleAuthenticatorJpaTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link GoogleAuthenticatorJpaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    JpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    MariaDbGoogleAuthenticatorJpaTokenRepositoryTests.class,
    MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorJpaTokenRepositoryTests.class
})
public class GoogleAuthenticatorJpaTestsSuite {
}
