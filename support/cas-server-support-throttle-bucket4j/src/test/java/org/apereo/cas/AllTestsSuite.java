package org.apereo.cas;

import org.apereo.cas.web.Bucket4jThrottledRequestExecutorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses(
    Bucket4jThrottledRequestExecutorTests.class
)
@Suite
public class AllTestsSuite {
}
