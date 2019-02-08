package org.apereo.cas.uma;

import org.apereo.cas.uma.ticket.resource.repository.JpaResourceSetRepositoryTests;
import org.apereo.cas.uma.web.controllers.authz.UmaAuthorizationRequestEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.permission.UmaPermissionRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.policy.UmaCreatePolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.policy.UmaDeletePolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.policy.UmaFindPolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.policy.UmaUpdatePolicyForResourceSetEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.resource.UmaCreateResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.resource.UmaDeleteResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.resource.UmaFindResourceSetRegistrationEndpointControllerTests;
import org.apereo.cas.uma.web.controllers.resource.UmaUpdateResourceSetRegistrationEndpointControllerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link UmaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    UmaPermissionRegistrationEndpointControllerTests.class,
    UmaCreateResourceSetRegistrationEndpointControllerTests.class,
    UmaDeleteResourceSetRegistrationEndpointControllerTests.class,
    UmaFindResourceSetRegistrationEndpointControllerTests.class,
    UmaUpdateResourceSetRegistrationEndpointControllerTests.class,
    UmaCreatePolicyForResourceSetEndpointControllerTests.class,
    UmaDeletePolicyForResourceSetEndpointControllerTests.class,
    UmaFindPolicyForResourceSetEndpointControllerTests.class,
    UmaUpdatePolicyForResourceSetEndpointControllerTests.class,
    UmaAuthorizationRequestEndpointControllerTests.class,
    JpaResourceSetRepositoryTests.class
})
public class UmaTestsSuite {
}
