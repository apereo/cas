package org.apereo.cas;

import org.apereo.cas.gauth.credential.MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorMongoDbTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({MongoDbGoogleAuthenticatorTokenCredentialRepositoryTests.class,
    GoogleAuthenticatorMongoDbTokenRepositoryTests.class})
public class AllTestsSuite {
}
