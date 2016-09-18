package org.apereo.cas.support.oauth;

import org.apereo.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenControllerTests;
import org.apereo.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({OAuth20AccessTokenControllerTests.class, OAuth20AuthorizeControllerTests.class,
                     OAuth20ProfileControllerTests.class})
/**
 * OAuth test suite that runs all test in a batch.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuthTestSuite {}
