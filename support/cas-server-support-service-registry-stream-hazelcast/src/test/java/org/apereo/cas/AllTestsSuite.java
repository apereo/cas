package org.apereo.cas;

import org.apereo.cas.config.CasServicesStreamingHazelcastConfigurationTests;
import org.apereo.cas.services.RegisteredServiceHazelcastDistributedCacheManagerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    RegisteredServiceHazelcastDistributedCacheManagerTests.class,
    CasServicesStreamingHazelcastConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
