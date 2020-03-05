package org.apereo.cas;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllCasConfigurationServerTestsSuite {
}
