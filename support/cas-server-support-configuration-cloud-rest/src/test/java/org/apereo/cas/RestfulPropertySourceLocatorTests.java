package org.apereo.cas;

import org.apereo.cas.config.RestfulPropertySourceLocator;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import java.util.Map;
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
    void verifyNoUrl() {
        val environment = new MockEnvironment();
        val loc = new RestfulPropertySourceLocator();
        assertTrue(((Map) loc.locate(environment).getSource()).isEmpty());
    }

    @Test
    void verifyBadParsing() {
        val loc = new RestfulPropertySourceLocator();
        try (val webServer = new MockWebServer("@@")) {
            webServer.start();
            val environment = new MockEnvironment();
            environment.setProperty(RestfulPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".url", "http://localhost:" + webServer.getPort());
            environment.setProperty(RestfulPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".basic-auth-username", "casuser");
            environment.setProperty(RestfulPropertySourceLocator.CAS_CONFIGURATION_PREFIX + ".basic-auth-password", "password");

            assertTrue(((Map) loc.locate(environment).getSource()).isEmpty());
        }
    }

}
