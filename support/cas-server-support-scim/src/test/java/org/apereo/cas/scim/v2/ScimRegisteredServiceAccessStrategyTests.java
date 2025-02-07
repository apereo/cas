package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.scim.v2.access.ScimRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.BaseScimTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("SCIM")
@ExtendWith(CasTestExtension.class)
class ScimRegisteredServiceAccessStrategyTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.scim.target=http://localhost:9666/scim/v2",
        "cas.scim.username=scim-user",
        "cas.scim.password=changeit"
    })
    @EnabledIfListeningOnPort(port = 9666)
    class ScimServerTests extends BaseScimTests {
        @Test
        void checkAuthorizationFails() throws Throwable {
            val attributes = new HashMap<String, Set<String>>();
            attributes.put("scimGroups", Set.of("admin"));

            val strategy = new ScimRegisteredServiceAccessStrategy();
            strategy.setRequiredAttributes(attributes);

            val request = RegisteredServiceAccessStrategyRequest.builder()
                .service(RegisteredServiceTestUtils.getService())
                .principalId(UUID.randomUUID().toString())
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .attributes(CoreAuthenticationTestUtils.getAttributes())
                .applicationContext(applicationContext)
                .build();
            assertFalse(strategy.authorizeRequest(request));
        }
    }
    
    @Nested
    @TestPropertySource(properties = {
        "cas.scim.target=http://localhost:${random.int[3000,9000]}",
        "cas.scim.username=scim-user",
        "cas.scim.password=changeit"
    })
    class MockServerTests extends BaseScimTests {

        private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(true).build().toObjectMapper();

        @Test
        void verifySerializeToJson() throws IOException {
            val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
            val attributes = new HashMap<String, Set<String>>();
            attributes.put("scimGroups", Set.of("Tour Guides"));
            val strategyWritten = new ScimRegisteredServiceAccessStrategy();
            strategyWritten.setRequiredAttributes(attributes);
            MAPPER.writeValue(jsonFile, strategyWritten);
            val credentialRead = MAPPER.readValue(jsonFile, ScimRegisteredServiceAccessStrategy.class);
            assertEquals(strategyWritten, credentialRead);
        }
        
        @Test
        void checkAuthorizationFails() throws Throwable {
            val attributes = new HashMap<String, Set<String>>();
            attributes.put("scimGroups", Set.of("Tour Guides"));

            val strategy = new ScimRegisteredServiceAccessStrategy();
            strategy.setRequiredAttributes(attributes);

            val request = RegisteredServiceAccessStrategyRequest.builder()
                .service(RegisteredServiceTestUtils.getService())
                .principalId("casuser@example.com")
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .attributes(CoreAuthenticationTestUtils.getAttributes())
                .applicationContext(applicationContext)
                .build();

            val port = URI.create(casProperties.getScim().getTarget()).getPort();
            try (val webServer = new MockWebServer(port, new ClassPathResource("scim-user.json"), "application/scim+json")) {
                webServer.start();
                assertTrue(strategy.authorizeRequest(request));
            }
        }
    }

}
