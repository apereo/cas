package org.apereo.cas.web.support;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CoreCookieTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CookieRetrievingCookieGeneratorTests.class,
    DefaultCasCookieValueManagerTests.class
})
@RunWith(JUnitPlatform.class)
public class CoreCookieTestsSuite {
}
