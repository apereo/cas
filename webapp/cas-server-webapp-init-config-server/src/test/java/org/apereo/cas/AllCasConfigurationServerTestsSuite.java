package org.apereo.cas;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasConfigurationServerTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasConfigurationServerBannerTests.class,
    CasConfigurationServerServletInitializerTests.class
})
@Suite
public class AllCasConfigurationServerTestsSuite {
}
