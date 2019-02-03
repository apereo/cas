package org.apereo.cas.web.support;

import org.junit.platform.suite.api.SelectClasses;

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
public class CoreCookieTestsSuite {
}
