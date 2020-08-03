package org.apereo.cas;

import org.apereo.cas.support.rest.RegisteredServiceResourceTests;
import org.apereo.cas.support.rest.RestServicesConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
