package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.oidc.token.OidcIdTokenGeneratorServiceTests;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIActionTests;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupportTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link OidcTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    OidcIdTokenGeneratorServiceTests.class,
    OidcRegisteredServiceUIActionTests.class,
    OidcAuthorizationRequestSupportTests.class})
@Slf4j
public class OidcTestSuite {
}
