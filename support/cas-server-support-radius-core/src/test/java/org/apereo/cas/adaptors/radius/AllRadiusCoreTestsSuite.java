package org.apereo.cas.adaptors.radius;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllRadiusCoreTestsSuite {
}
