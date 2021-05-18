package org.apereo.cas.logging;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class CasLoggingApiAllTestsSuite {
}
