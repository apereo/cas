package org.apereo.cas.logging;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite for cloudwatch logs
 *
 * @since 6.2.0
 */
@SelectClasses({CloudWatchAppenderSpecTests.class, CloudWatchAppenderTests.class})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
