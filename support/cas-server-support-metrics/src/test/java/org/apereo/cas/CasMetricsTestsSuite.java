package org.apereo.cas;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CasMetricsConfigurationTests.class,
    SystemMonitorHealthIndicatorTests.class
})
@RunWith(JUnitPlatform.class)
public class CasMetricsTestsSuite {
}
