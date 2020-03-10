package org.apereo.cas.logging;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite for cloudwatch logs
 */
@SelectClasses({CloudWatchAppenderSpecTests.class, CloudWatchAppenderTests.class})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
