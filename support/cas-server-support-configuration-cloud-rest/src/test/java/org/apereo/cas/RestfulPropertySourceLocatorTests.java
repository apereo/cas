package org.apereo.cas;

import org.apereo.cas.config.RestfulPropertySourceLocator;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

/**
 * This is {@link RestfulPropertySourceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RestfulApi")
public class RestfulPropertySourceLocatorTests {

    @Test
    public void verifyNoUrl() {
        val environment = new MockEnvironment();
        val loc = new RestfulPropertySourceLocator();
        assertTrue(((Map) loc.locate(environment).getSource()).isEmpty());
    }

    @Test
    public void verifyBadParsing() {
        val environment = new MockEnvironment();
        environment.setProperty(RestfulPropertySourceLocator.CAS_CONFIGURATION_PREFIX + '.' + "url", "http://localhost:8021");
        val loc = new RestfulPropertySourceLocator();
        try (val webServer = new MockWebServer(8021,
            new ByteArrayResource("@@".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(((Map) loc.locate(environment).getSource()).isEmpty());
        }
    }

}
