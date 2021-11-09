package org.apereo.cas;

import org.apereo.cas.logout.ChainingSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.logout.DefaultLogoutManagerTests;
import org.apereo.cas.logout.DefaultLogoutRedirectionStrategyTests;
import org.apereo.cas.logout.DefaultSingleLogoutMessageCreatorTests;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilderTests;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandlerTests;
import org.apereo.cas.logout.LogoutHttpMessageTests;
import org.apereo.cas.logout.LogoutWebApplicationServiceFactoryTests;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreatorTests;
import org.apereo.cas.logout.config.CasCoreLogoutConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    LogoutWebApplicationServiceFactoryTests.class,
    CasCoreLogoutConfigurationTests.class,
    DefaultSingleLogoutServiceMessageHandlerTests.class,
    DefaultSingleLogoutMessageCreatorTests.class,
    SamlCompliantLogoutMessageCreatorTests.class
})
@Suite
public class CasLogoutTestsSuite {
}
