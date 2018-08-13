package org.apereo.cas;

import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceTests;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses(OAuthRegisteredServiceTests.class)
public class AllTestsSuite {
}
