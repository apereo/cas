package org.apereo.cas.services.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link ManagementWebappTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ManageRegisteredServicesMultiActionControllerTests.class,
        RegisteredServiceSimpleFormControllerTests.class})
public class ManagementWebappTestSuite {
}
