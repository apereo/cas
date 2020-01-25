package org.apereo.cas.authentication;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all MongoDb tests.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    MongoDbAuthenticationHandlerTests.class,
    MongoDbConnectionFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllMongoDbTestsSuite {
}
