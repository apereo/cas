package org.apereo.cas.tomcat;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllCasServletTomcatWebServerTestsSuite}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SelectClasses({
    CasTomcatServletWebServerFactoryCloudClusterTests.class,
    CasTomcatServletWebServerFactoryClusterTests.class,
    CasTomcatServletWebServerFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasServletTomcatWebServerTestsSuite {
}
