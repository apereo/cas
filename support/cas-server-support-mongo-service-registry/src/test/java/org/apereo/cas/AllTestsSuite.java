package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AllTestsSuite {
}
