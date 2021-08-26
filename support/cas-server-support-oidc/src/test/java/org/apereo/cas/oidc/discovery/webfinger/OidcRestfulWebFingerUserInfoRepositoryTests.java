package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcRestfulWebFingerUserInfoRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRestfulWebFingerUserInfoRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApi")
public class OidcRestfulWebFingerUserInfoRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private MockWebServer webServer;

    @Test
    public void verifyBadPayload() throws Exception {
        try (val webServer = new MockWebServer(9312,
            new ByteArrayResource("-@@-".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());
            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9312");
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByEmailAddress("cas@example.org");
            assertTrue(results.isEmpty());
        }
    }

    @Test
    public void verifyFindByEmail() throws Exception {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrap("email", "cas@example.org"));
        try (val webServer = new MockWebServer(9312,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9312");
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByEmailAddress("cas@example.org");
            assertNotNull(results);
            assertTrue(results.containsKey("email"));
            assertEquals("cas@example.org", results.get("email"));
        }
    }

    @Test
    public void verifyFindByUsername() throws Exception {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrap("username", "casuser"));
        try (val webServer = new MockWebServer(9312,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9312");
            val repo = new OidcRestfulWebFingerUserInfoRepository(props);
            val results = repo.findByUsername("casuser");
            assertNotNull(results);
            assertTrue(results.containsKey("username"));
            assertEquals("casuser", results.get("username"));
        }
    }
}
