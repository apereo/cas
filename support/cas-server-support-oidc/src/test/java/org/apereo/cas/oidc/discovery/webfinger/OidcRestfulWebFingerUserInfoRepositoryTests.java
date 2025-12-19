package org.apereo.cas.oidc.discovery.webfinger;

import module java.base;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcRestfulWebFingerUserInfoRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRestfulWebFingerUserInfoRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApi")
class OidcRestfulWebFingerUserInfoRepositoryTests {
    private MockWebServer webServer;

    @Test
    void verifyBadPayload() {
        try (val webServer = new MockWebServer("-@@-")) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());
            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByEmailAddress("cas@example.org");
            assertTrue(results.isEmpty());
        }
    }

    @Test
    void verifyFindByEmail() {
        try (val webServer = new MockWebServer(CollectionUtils.wrap("email", "cas@example.org"))) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(webServer.isRunning());

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByEmailAddress("cas@example.org");
            assertNotNull(results);
            assertTrue(results.containsKey("email"));
            assertEquals("cas@example.org", results.get("email"));
        }
    }

    @Test
    void verifyFindByUsername() {
        try (val webServer = new MockWebServer(CollectionUtils.wrap("username", "casuser"))) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());
            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByUsername("casuser");
            assertNotNull(results);
            assertTrue(results.containsKey("username"));
            assertEquals("casuser", results.get("username"));
        }
    }
}
