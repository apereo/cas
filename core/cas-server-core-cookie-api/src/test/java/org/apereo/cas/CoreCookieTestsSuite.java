package org.apereo.cas;

import org.apereo.cas.web.support.CookieRetrievingCookieGeneratorTests;
import org.apereo.cas.web.support.DefaultCasCookieValueManagerTests;
import org.apereo.cas.web.support.mgmr.EncryptedCookieValueManagerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CoreCookieTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    CookieRetrievingCookieGeneratorTests.class,
    DefaultCasCookieValueManagerTests.class,
    EncryptedCookieValueManagerTests.class
})
@Suite
public class CoreCookieTestsSuite {
}
