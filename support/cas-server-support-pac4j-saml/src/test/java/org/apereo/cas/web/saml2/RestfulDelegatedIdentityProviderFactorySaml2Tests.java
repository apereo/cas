package org.apereo.cas.web.saml2;

import org.apereo.cas.support.pac4j.clients.BaseDelegatedClientFactoryTests;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.config.client.PropertiesConstants;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulDelegatedIdentityProviderFactorySaml2Tests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
class RestfulDelegatedIdentityProviderFactorySaml2Tests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static Map<String, String> getProperties() {
        return Map.of(
            PropertiesConstants.SAML_IDENTITY_PROVIDER_METADATA_PATH, "file:/path/to/idp.xml",
            PropertiesConstants.SAML_KEYSTORE_PATH, "file:/path/to/keystore.jks",
            PropertiesConstants.SAML_KEYSTORE_PASSWORD, "p@$$w0rd",
            PropertiesConstants.SAML_PRIVATE_KEY_PASSWORD, "p@$$w0rd",
            PropertiesConstants.SAML_SERVICE_PROVIDER_ENTITY_ID, "example-entity-id",
            PropertiesConstants.SAML_SERVICE_PROVIDER_METADATA_PATH, "file:/path/to/sp.xml");
    }

    @TestPropertySource(properties = "cas.custom.properties.delegation-test.enabled=false")
    abstract static class BaseTests extends BaseDelegatedClientFactoryTests {
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.lazy-init=true",
        "cas.authn.pac4j.rest.url=http://localhost:9212"
    })
    class DefaultTests extends BaseTests {
        @Test
        void verifyAction() throws Throwable {
            val clients = new HashMap<String, Object>();
            clients.put("callbackUrl", "https://sso.example.org/cas/login");
            clients.put("properties", getProperties());

            val entity = MAPPER.writeValueAsString(clients);
            try (val webServer = new MockWebServer(9212,
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
