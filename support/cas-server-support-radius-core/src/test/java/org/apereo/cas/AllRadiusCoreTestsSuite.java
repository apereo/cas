package org.apereo.cas;

import org.apereo.cas.adaptors.radius.BlockingRadiusServerRadSecTransportTests;
import org.apereo.cas.adaptors.radius.BlockingRadiusServerTests;
import org.apereo.cas.adaptors.radius.NonBlockingRadiusServerTests;
import org.apereo.cas.adaptors.radius.RadiusProtocolTests;
import org.apereo.cas.adaptors.radius.RadiusUtilsTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllRadiusCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    BlockingRadiusServerTests.class,
    RadiusProtocolTests.class,
    NonBlockingRadiusServerTests.class,
    RadiusUtilsTests.class,
    BlockingRadiusServerRadSecTransportTests.class
})
@Suite
public class AllRadiusCoreTestsSuite {
}
