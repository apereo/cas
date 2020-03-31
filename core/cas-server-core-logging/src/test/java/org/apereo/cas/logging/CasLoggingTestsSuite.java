package org.apereo.cas.logging;

import org.apereo.cas.logging.web.ThreadContextMDCServletFilterTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasLoggingTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses(
    ThreadContextMDCServletFilterTests.class
)
@RunWith(JUnitPlatform.class)
public class CasLoggingTestsSuite {
}
