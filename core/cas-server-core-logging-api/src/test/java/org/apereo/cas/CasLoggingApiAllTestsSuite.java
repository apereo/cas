package org.apereo.cas;

import org.apereo.cas.logging.CasAppenderTests;
import org.apereo.cas.logging.ExceptionOnlyFilterTests;
import org.apereo.cas.logging.Log4jInitializationTests;

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
    ExceptionOnlyFilterTests.class,
    Log4jInitializationTests.class
})
@Suite
public class CasLoggingApiAllTestsSuite {
}
