package org.apereo.cas;

import org.apereo.cas.services.MongoServiceRegistryDaoCloudTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas mongo test cases.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(MongoServiceRegistryDaoCloudTests.class)
public class AllTestsSuite {
}
