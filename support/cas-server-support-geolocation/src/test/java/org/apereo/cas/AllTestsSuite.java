package org.apereo.cas;

import org.apereo.cas.nativex.CasGeoLocationRuntimeHintsTests;
import org.apereo.cas.support.geo.GeoLocationServiceTests;
import org.apereo.cas.support.geo.GroovyGeoLocationServiceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CasGeoLocationRuntimeHintsTests.class,
    GeoLocationServiceTests.class,
    GroovyGeoLocationServiceTests.class
})
@Suite
public class AllTestsSuite {
}
