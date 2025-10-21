package org.apereo.cas.support.pac4j.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.config.client.PropertiesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulDelegatedIdentityProviderFactoryOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
class RestfulDelegatedIdentityProviderFactoryOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static Map<String, String> getProperties() {
        return Map.of(
            PropertiesConstants.GITHUB_ID, "id",
            PropertiesConstants.GITHUB_SECRET, "secret");
    }

    @TestPropertySource(properties = "cas.custom.properties.delegation-test.enabled=false")
    abstract static class BaseTests extends BaseDelegatedClientFactoryTests {
        @Autowired
        protected CasConfigurationProperties casProperties;
        
    }
 
    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.lazy-init=true",
        "cas.authn.pac4j.rest.url=http://localhost:${random.int[3000,9000]}"
    })
    class DefaultTests extends BaseTests {
        @Test
        void verifyAction() throws Throwable {
            val clients = new HashMap<String, Object>();
            clients.put("callbackUrl", "https://sso.example.org/cas/login");
            clients.put("properties", getProperties());

            val entity = MAPPER.writeValueAsString(clients);
            val props = casProperties.getAuthn().getPac4j().getRest();
            val port = URI.create(props.getUrl()).getPort();
            try (val webServer = new MockWebServer(port,
                new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();

                var clientsFound = delegatedIdentityProviderFactory.build();
                assertNotNull(clientsFound);
                assertEquals(1, clientsFound.size());

                /*
                 * Try the cache once the list is retrieved...
                 */
                clientsFound = delegatedIdentityProviderFactory.build();
                assertNotNull(clientsFound);
                assertEquals(1, clientsFound.size());
            }
        }
    }

}
