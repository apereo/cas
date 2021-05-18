package org.apereo.cas;

import org.apereo.cas.adaptors.swivel.SwivelAuthenticationHandlerTests;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowActionTests;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.swivel.web.flow.rest.SwivelTuringImageGeneratorControllerTests;
import org.apereo.cas.config.SwivelAuthenticationMultifactorProviderBypassConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllSwivelTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    SwivelAuthenticationHandlerTests.class,
    SwivelAuthenticationWebflowActionTests.class,
    SwivelAuthenticationWebflowEventResolverTests.class,
    SwivelAuthenticationMultifactorProviderBypassConfigurationTests.class,
    SwivelTuringImageGeneratorControllerTests.class,
    SwivelMultifactorWebflowConfigurerTests.class
})
@Suite
public class AllSwivelTestsSuite {
}
