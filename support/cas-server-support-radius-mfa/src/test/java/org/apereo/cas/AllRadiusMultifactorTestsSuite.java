package org.apereo.cas;

import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandlerTests;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolverFailureTests;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurerTests;
import org.apereo.cas.config.support.authentication.RadiusTokenAuthenticationMultifactorProviderBypassConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllRadiusMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SelectClasses({
    RadiusTokenAuthenticationHandlerTests.class,
    RadiusAuthenticationWebflowEventResolverFailureTests.class,
    RadiusAuthenticationWebflowEventResolverTests.class,
    RadiusTokenAuthenticationMultifactorProviderBypassConfigurationTests.class,
    RadiusMultifactorWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllRadiusMultifactorTestsSuite {
}
