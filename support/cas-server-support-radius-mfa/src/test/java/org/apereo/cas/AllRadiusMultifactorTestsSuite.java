package org.apereo.cas;

import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandlerTests;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurerTests;

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
    RadiusMultifactorWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllRadiusMultifactorTestsSuite {
}
