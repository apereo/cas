package org.apereo.cas.adaptors.radius;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllRadiusCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    BlockingRadiusServerTests.class,
    NonBlockingRadiusServerTests.class,
    RadiusUtilsTests.class
})
@RunWith(JUnitPlatform.class)
public class AllRadiusCoreTestsSuite {
}
