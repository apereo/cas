package org.apereo.cas.logout;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link CasLogoutTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    DefaultLogoutManagerTests.class,
    DefaultSingleLogoutServiceLogoutUrlBuilderTests.class,
    LogoutHttpMessageTests.class,
    SamlCompliantLogoutMessageCreatorTests.class
})
public class CasLogoutTestsSuite {
}
