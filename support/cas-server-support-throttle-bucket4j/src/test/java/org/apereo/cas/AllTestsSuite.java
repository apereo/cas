package org.apereo.cas;

import org.apereo.cas.web.Bucket4jBlockingThrottledRequestExecutorTests;
import org.apereo.cas.web.Bucket4jThrottledRequestExecutorTests;

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
    Bucket4jThrottledRequestExecutorTests.class,
    Bucket4jBlockingThrottledRequestExecutorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
