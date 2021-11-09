package org.apereo.cas;

import org.apereo.cas.web.CasWebApplicationServletInitializerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTomcatWebappTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    CasWebApplicationServletInitializerTests.class,
    CasTomcatBannerTests.class
})
@Suite
public class AllTomcatWebappTestsSuite {
}
