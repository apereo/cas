package org.apereo.cas;

import org.apereo.cas.services.MongoDbServiceRegistryCloudTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas mongo test cases.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses(MongoDbServiceRegistryCloudTests.class)
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
