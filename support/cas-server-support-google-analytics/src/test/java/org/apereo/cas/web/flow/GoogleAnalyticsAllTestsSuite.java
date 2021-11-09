package org.apereo.cas.web.flow;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link GoogleAnalyticsAllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    CreateGoogleAnalyticsCookieActionTests.class,
    CasGoogleAnalyticsCookieGeneratorTests.class,
    RemoveGoogleAnalyticsCookieActionTests.class,
    CasGoogleAnalyticsWebflowConfigurerTests.class
})
@Suite
public class GoogleAnalyticsAllTestsSuite {
}
