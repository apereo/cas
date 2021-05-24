package org.apereo.cas.support.inwebo;

import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationDeviceMetadataPopulatorTests;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandlerTests;
import org.apereo.cas.support.inwebo.authentication.InweboCredentialTests;
import org.apereo.cas.support.inwebo.config.InweboConfigurationTests;
import org.apereo.cas.support.inwebo.service.InweboConsoleAdminTests;
import org.apereo.cas.support.inwebo.service.InweboServiceTests;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorWebflowConfigurerTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckAuthenticationActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckUserActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboMustEnrollActionTests;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboPushAuthenticateActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is all the Inwebo tests.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@SelectClasses({
    InweboCheckAuthenticationActionTests.class,
    InweboAuthenticationHandlerTests.class,
    InweboCheckUserActionTests.class,
    InweboMustEnrollActionTests.class,
    InweboConfigurationTests.class,
    InweboPushAuthenticateActionTests.class,
    InweboAuthenticationDeviceMetadataPopulatorTests.class,
    InweboMultifactorAuthenticationProviderTests.class,
    InweboCredentialTests.class,
    InweboConsoleAdminTests.class,
    InweboServiceTests.class,
    InweboMultifactorWebflowConfigurerTests.class
})
@Suite
public class AllInweboTestsSuite {
}
