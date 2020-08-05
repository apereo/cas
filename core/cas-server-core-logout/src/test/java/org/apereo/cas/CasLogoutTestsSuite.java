package org.apereo.cas;

import org.apereo.cas.logout.ChainingSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.logout.DefaultLogoutManagerTests;
import org.apereo.cas.logout.DefaultLogoutRedirectionStrategyTests;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.logout.LogoutHttpMessageTests;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreatorTests;

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
    DefaultLogoutRedirectionStrategyTests.class,
    ChainingSingleLogoutServiceLogoutUrlBuilderTests.class,
    DefaultSingleLogoutServiceLogoutUrlBuilderTests.class,
    LogoutHttpMessageTests.class,
    SamlCompliantLogoutMessageCreatorTests.class
})
@RunWith(JUnitPlatform.class)
public class CasLogoutTestsSuite {
}
