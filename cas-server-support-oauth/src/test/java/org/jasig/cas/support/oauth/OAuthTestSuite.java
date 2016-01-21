package org.jasig.cas.support.oauth;

import org.jasig.cas.support.oauth.web.OAuth20AccessTokenControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20AuthorizeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20CallbackAuthorizeControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20ProfileControllerTests;
import org.jasig.cas.support.oauth.web.OAuth20WrapperControllerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({OAuth20AccessTokenControllerTests.class, OAuth20AuthorizeControllerTests.class,
                     OAuth20CallbackAuthorizeControllerTests.class, OAuth20ProfileControllerTests.class,
                     OAuth20WrapperControllerTests.class})
/**
 * OAuth test suite that runs all test in a batch.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuthTestSuite {}
