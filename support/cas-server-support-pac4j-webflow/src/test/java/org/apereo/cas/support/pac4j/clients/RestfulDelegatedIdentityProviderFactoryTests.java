package org.apereo.cas.support.pac4j.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.config.client.PropertiesConstants;
import org.pac4j.core.client.IndirectClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulDelegatedIdentityProviderFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
class RestfulDelegatedIdentityProviderFactoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static Map<String, String> getProperties() {
        return Map.of(
            PropertiesConstants.CAS_PROTOCOL + ".1", "CAS20",
            PropertiesConstants.CAS_LOGIN_URL + ".1", "https://example.com/cas/login",
            PropertiesConstants.CAS_PROTOCOL + ".2", "CAS30",
            PropertiesConstants.CAS_LOGIN_URL + ".2", "https://example.com/cas/login");
    }

    @TestPropertySource(properties = "cas.custom.properties.delegation-test.enabled=false")
    abstract static class BaseTests extends BaseDelegatedClientFactoryTests {
        @Autowired
        protected CasConfigurationProperties casProperties;
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.lazy-init=false",
        "cas.authn.pac4j.rest.url=http://localhost:${random.int[3000,9000]}"
    })
    class InvalidStatusCodeTests extends BaseTests {
        @Test
        void verifyBadStatusCode() {
            val props = casProperties.getAuthn().getPac4j().getRest();
            val port = URI.create(props.getUrl()).getPort();
            try (val webServer = new MockWebServer(port, HttpStatus.EXPECTATION_FAILED)) {
                webServer.start();
                val clientsFound = delegatedIdentityProviderFactory.build();
                assertNotNull(clientsFound);
                assertTrue(clientsFound.isEmpty());
            }
        }
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
                assertEquals(2, clientsFound.size());

                /*
                 * Try the cache once the list is retrieved...
                 */
                clientsFound = delegatedIdentityProviderFactory.build();
                assertNotNull(clientsFound);
                assertEquals(2, clientsFound.size());
            }
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.server.name=https://sso.example.org",
        "cas.server.prefix=${cas.server.name}/cas",
        "cas.authn.pac4j.core.lazy-init=false",
        "cas.authn.pac4j.rest.url=http://localhost:${random.int[3000,9000]}",
        "cas.authn.pac4j.rest.type=cas"
    })
    class CasPropertiesTests extends BaseTests {

        @Test
        void verifyAction() throws Throwable {
            val clients = new HashMap<String, Object>();
            clients.put("cas.authn.pac4j.cas[0].login-url", "https://localhost:8444/cas/login");
            clients.put("cas.authn.pac4j.cas[0].protocol", "CAS30");

            val entity = MAPPER.writeValueAsString(clients);
            val props = casProperties.getAuthn().getPac4j().getRest();
            val port = URI.create(props.getUrl()).getPort();
            try (val webServer = new MockWebServer(port,
                new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
                webServer.start();

                var clientsFound = List.copyOf(delegatedIdentityProviderFactory.build());
                assertNotNull(clientsFound);
                assertEquals(1, clientsFound.size());
                assertEquals(casProperties.getServer().getLoginUrl(), ((IndirectClient) clientsFound.getFirst()).getCallbackUrl());

                /*
                 * Try the cache once the list is retrieved...
                 */
                clientsFound = List.copyOf(delegatedIdentityProviderFactory.build());
                assertNotNull(clientsFound);
                assertEquals(1, clientsFound.size());
            }
        }
    }

}
