package org.apereo.cas.logout;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link CasLogoutTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DefaultLogoutManagerTests.class,
    DefaultSingleLogoutServiceLogoutUrlBuilderTests.class,
    LogoutHttpMessageTests.class,
    SamlCompliantLogoutMessageCreatorTests.class
})
public class CasLogoutTestSuite {
}
