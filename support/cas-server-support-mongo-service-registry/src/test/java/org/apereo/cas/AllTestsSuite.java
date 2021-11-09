package org.apereo.cas;

import org.apereo.cas.services.MongoDbServiceRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas mongo test cases.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses(MongoDbServiceRegistryTests.class)
@Suite
public class AllTestsSuite {
}
