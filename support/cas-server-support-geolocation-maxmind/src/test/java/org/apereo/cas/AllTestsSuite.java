package org.apereo.cas;

import org.apereo.cas.support.geo.maxmind.MaxmindDatabaseGeoLocationServiceTests;
import org.apereo.cas.support.geo.maxmind.config.CasGeoLocationMaxmindConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class AllTestsSuite {
}
