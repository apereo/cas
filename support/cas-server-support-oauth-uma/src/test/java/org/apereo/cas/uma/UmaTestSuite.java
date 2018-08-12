package org.apereo.cas.uma;

import org.apereo.cas.uma.web.controllers.UmaPermissionRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.UmaCreateResourceSetRegistrationEndpointControllerTests;

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
    UmaCreateResourceSetRegistrationEndpointControllerTests.class
})
public class UmaTestSuite {
}
