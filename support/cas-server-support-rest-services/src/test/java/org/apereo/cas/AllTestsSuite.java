package org.apereo.cas;

import org.apereo.cas.support.rest.RegisteredServiceResourceTests;
import org.apereo.cas.support.rest.RestServicesConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RegisteredServiceResourceTests.class,
    RestServicesConfigurationTests.class
})
@Suite
public class AllTestsSuite {
}
