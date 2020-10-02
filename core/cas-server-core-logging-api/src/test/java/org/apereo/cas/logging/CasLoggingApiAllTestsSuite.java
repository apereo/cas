package org.apereo.cas.logging;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasLoggingApiAllTestsSuite}.
 *
 * @author Hal Deadman
 * @since 6.3.0
 */
@SelectClasses({
    CasAppenderTests.class,
    ExceptionOnlyFilterTests.class
})
@RunWith(JUnitPlatform.class)
public class CasLoggingApiAllTestsSuite {
}
