package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.services.OAuthRegisteredServiceTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({OAuthRegisteredServiceTests.class})
/**
 * OAuth test suite that runs all test in a batch.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuthTestSuite {}
