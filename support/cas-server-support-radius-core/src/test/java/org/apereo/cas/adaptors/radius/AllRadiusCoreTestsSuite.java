package org.apereo.cas.adaptors.radius;

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
    RadiusUtilsTests.class
})
@Suite
public class AllRadiusCoreTestsSuite {
}
