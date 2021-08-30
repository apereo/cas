package org.apereo.cas.gauth;

import org.apereo.cas.gauth.credential.DynamoDbGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorDynamoDbTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    DynamoDbGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorDynamoDbTokenRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}
