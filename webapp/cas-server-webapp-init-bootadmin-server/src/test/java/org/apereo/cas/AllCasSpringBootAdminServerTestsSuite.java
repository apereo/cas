package org.apereo.cas;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasSpringBootAdminServerTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasSpringBootAdminServerBannerTests.class,
    CasSpringBootAdminServerServletInitializerTests.class
})
@Suite
public class AllCasSpringBootAdminServerTestsSuite {
}
