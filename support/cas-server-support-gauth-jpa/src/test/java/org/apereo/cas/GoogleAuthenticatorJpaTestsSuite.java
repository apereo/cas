package org.apereo.cas;

import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.MariaDbJpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.MySQLJpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.OracleJpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepositoryTests;
import org.apereo.cas.gauth.token.MariaDbGoogleAuthenticatorJpaTokenRepositoryTests;
import org.apereo.cas.gauth.token.MySQLGoogleAuthenticatorJpaTokenRepositoryTests;
import org.apereo.cas.gauth.token.OracleGoogleAuthenticatorJpaTokenRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    OracleJpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorJpaTokenRepositoryTests.class,
    MySQLJpaGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    MySQLGoogleAuthenticatorJpaTokenRepositoryTests.class,
    OracleGoogleAuthenticatorJpaTokenRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class GoogleAuthenticatorJpaTestsSuite {
}
