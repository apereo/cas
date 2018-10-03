package org.apereo.cas.adaptors.radius;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllRadiusCoreTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BlockingRadiusServerTests.class,
    NonBlockingRadiusServerTests.class,
    RadiusUtilsTests.class
})
public class AllRadiusCoreTestsSuite {
}
