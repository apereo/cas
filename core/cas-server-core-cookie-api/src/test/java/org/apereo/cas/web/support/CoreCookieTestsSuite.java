package org.apereo.cas.web.support;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link CoreCookieTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CookieRetrievingCookieGeneratorTests.class,
    DefaultCasCookieValueManagerTests.class
})
public class CoreCookieTestsSuite {
}
