package org.apereo.cas;

import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * OAuth test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SelectClasses(OAuthRegisteredServiceTests.class)
public class AllTestsSuite {
}
