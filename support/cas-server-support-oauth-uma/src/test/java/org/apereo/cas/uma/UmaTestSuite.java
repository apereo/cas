package org.apereo.cas.uma;

import org.apereo.cas.uma.web.controllers.UmaCreatePolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaCreateResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaDeletePolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaDeleteResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaFindPolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaFindResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaPermissionRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaUpdateResourceSetRegistrationEndpointControllerTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link UmaTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    UmaPermissionRegistrationEndpointControllerTests.class,
    UmaCreateResourceSetRegistrationEndpointControllerTests.class,
    UmaDeleteResourceSetRegistrationEndpointControllerTests.class,
    UmaFindResourceSetRegistrationEndpointControllerTests.class,
    UmaUpdateResourceSetRegistrationEndpointControllerTests.class,
    UmaCreatePolicyForResourceSetEndpointControllerTests.class,
    UmaDeletePolicyForResourceSetEndpointControllerTests.class,
    UmaFindPolicyForResourceSetEndpointControllerTests.class
})
public class UmaTestSuite {
}
