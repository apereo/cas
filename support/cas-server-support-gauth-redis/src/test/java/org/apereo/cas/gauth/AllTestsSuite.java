package org.apereo.cas.gauth;

import org.apereo.cas.gauth.credential.RedisGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorRedisTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link org.apereo.cas.AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    GoogleAuthenticatorRedisTokenRepositoryTests.class,
    RedisGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
public class AllTestsSuite {
}
