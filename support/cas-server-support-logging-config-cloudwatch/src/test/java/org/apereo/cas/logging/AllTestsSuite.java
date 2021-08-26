package org.apereo.cas.logging;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for cloudwatch logs
 *
 * @since 6.2.0
 */
@SelectClasses({
    CloudWatchAppenderSpecTests.class,
    CloudWatchAppenderTests.class
})
@Suite
public class AllTestsSuite {
}
