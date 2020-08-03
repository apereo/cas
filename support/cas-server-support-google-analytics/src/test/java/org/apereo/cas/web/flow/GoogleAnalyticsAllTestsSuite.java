package org.apereo.cas.web.flow;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class GoogleAnalyticsAllTestsSuite {
}
