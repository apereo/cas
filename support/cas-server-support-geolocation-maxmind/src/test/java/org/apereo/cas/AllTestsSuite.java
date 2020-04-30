package org.apereo.cas;

import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationServiceTests;
import org.apereo.cas.support.geo.maxmind.config.CasGeoLocationMaxmindConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    MaxmindDatabaseGeoLocationServiceTests.class,
    CasGeoLocationMaxmindConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
