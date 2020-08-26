package org.apereo.cas;

import org.apereo.cas.config.CasServicesStreamingKafkaConfigurationTests;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheListenerTests;
import org.apereo.cas.services.RegisteredServiceKafkaDistributedCacheManagerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all tests.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SelectClasses({
    RegisteredServiceKafkaDistributedCacheManagerTests.class,
    RegisteredServiceKafkaDistributedCacheListenerTests.class,
    CasServicesStreamingKafkaConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
