package org.apereo.cas;

import org.apereo.cas.gauth.credential.ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.credential.LdapGoogleAuthenticatorTokenCredentialRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link GoogleAuthenticatorLdapTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    LdapGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    ActiveDirectoryGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class GoogleAuthenticatorLdapTestsSuite {
}
