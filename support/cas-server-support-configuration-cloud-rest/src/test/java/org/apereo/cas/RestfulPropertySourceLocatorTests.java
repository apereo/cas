package org.apereo.cas;

import module java.base;
import org.apereo.cas.config.RestfulPropertySource;
import org.apereo.cas.config.RestfulPropertySourceLocator;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPropertySourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RestfulApi")
class RestfulPropertySourceLocatorTests {
    @Test
    void verifyBadParsing() {
        try (val webServer = new MockWebServer("@@")) {
            webServer.start();
            val environment = new MockEnvironment();
            environment.setProperty(RestfulPropertySource.CAS_CONFIGURATION_PREFIX + ".url", "http://localhost:" + webServer.getPort());
            environment.setProperty(RestfulPropertySource.CAS_CONFIGURATION_PREFIX + ".basic-auth-username", "casuser");
            environment.setProperty(RestfulPropertySource.CAS_CONFIGURATION_PREFIX + ".basic-auth-password", "password");
            val propertySource = new RestfulPropertySourceLocator().locate(environment);
            assertNull(propertySource.getProperty("some-random-property"));
        }
    }

}
