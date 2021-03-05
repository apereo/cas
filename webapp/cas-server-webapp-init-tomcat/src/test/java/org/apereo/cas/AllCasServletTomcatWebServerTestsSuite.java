package org.apereo.cas;

import org.apereo.cas.config.CasEmbeddedContainerTomcatFiltersConfigurationTests;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactoryCloudClusterTests;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactoryClusterTests;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactoryCustomizerTests;
import org.apereo.cas.tomcat.CasTomcatServletWebServerFactoryTests;

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
    CasTomcatServletWebServerFactoryCustomizerTests.class,
    CasEmbeddedContainerTomcatFiltersConfigurationTests.class,
    CasTomcatServletWebServerFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasServletTomcatWebServerTestsSuite {
}
