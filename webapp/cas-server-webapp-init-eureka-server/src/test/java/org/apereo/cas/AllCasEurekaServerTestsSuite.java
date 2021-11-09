package org.apereo.cas;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasEurekaServerTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasEurekaServerBannerTests.class,
    CasEurekaServerServletInitializerTests.class
})
@Suite
public class AllCasEurekaServerTestsSuite {
}
