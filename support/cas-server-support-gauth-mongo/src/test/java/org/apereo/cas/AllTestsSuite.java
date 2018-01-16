package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMongoDbTokenCredentialRepositoryTests;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMongoDbTokenRepositoryTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({GoogleAuthenticatorMongoDbTokenCredentialRepositoryTests.class,
        GoogleAuthenticatorMongoDbTokenRepositoryTests.class})
@Slf4j
public class AllTestsSuite {
}
