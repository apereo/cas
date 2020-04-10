package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.config.client.PropertiesConstants;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
public class RestfulDelegatedClientFactoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static Map<String, String> getProperties() {
        return Map.of(
            PropertiesConstants.SAML_IDENTITY_PROVIDER_METADATA_PATH, "file:/path/to/idp.xml",
            PropertiesConstants.SAML_KEYSTORE_PATH, "file:/path/to/keystore.jks",
            PropertiesConstants.SAML_KEYSTORE_PASSWORD, "p@$$w0rd",
            PropertiesConstants.SAML_PRIVATE_KEY_PASSWORD, "p@$$w0rd",
            PropertiesConstants.SAML_SERVICE_PROVIDER_ENTITY_ID, "example-entity-id",
            PropertiesConstants.SAML_SERVICE_PROVIDER_METADATA_PATH, "file:/path/to/sp.xml",

            PropertiesConstants.CAS_PROTOCOL + ".1", "CAS20",
            PropertiesConstants.CAS_LOGIN_URL + ".1", "https://example.com/cas/login",

            PropertiesConstants.GITHUB_ID, "id",
            PropertiesConstants.GITHUB_SECRET, "secret");
    }

    @Test
    public void verifyAction() throws Exception {
        val clients = new HashMap<String, Object>();
        clients.put("callbackUrl", "https://sso.example.org/cas/login");
        clients.put("properties", getProperties());

        val entity = MAPPER.writeValueAsString(clients);
        try (val webServer = new MockWebServer(9212,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getPac4j().getRest().setUrl("http://localhost:9212");
            val r = new RestfulDelegatedClientFactory(props);
            val res = r.build();
            assertNotNull(res);
            assertEquals(3, res.size());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
