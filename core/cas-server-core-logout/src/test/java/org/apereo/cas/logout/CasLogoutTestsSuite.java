package org.apereo.cas.logout;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class CasLogoutTestsSuite {
}
