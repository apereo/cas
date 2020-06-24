package org.apereo.cas;

import org.apereo.cas.config.CasServicesStreamingKafkaConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all tests.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SelectClasses(CasServicesStreamingKafkaConfigurationTests.class)
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
