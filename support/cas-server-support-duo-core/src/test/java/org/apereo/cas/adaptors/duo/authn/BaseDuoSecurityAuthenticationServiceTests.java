package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseDuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
    "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
    "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
    "cas.authn.mfa.duo[0].duo-api-host=http://localhost:6556"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class BaseDuoSecurityAuthenticationServiceTests {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;

    @Test
    public void verifyPingOK() throws Exception {
        val results = MAPPER.writeValueAsString(Map.of("response", "pong", "stat", "OK"));
        try (val webServer = new MockWebServer(6556,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "Output"),
            HttpStatus.OK)) {
            webServer.start();
            val service = new BasicDuoSecurityAuthenticationService(
                casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
            assertTrue(service.ping());
            assertNotNull(service.getApiHost());
        }
    }

    @Test
    public void verifyPingFails() throws Exception {
        val results = MAPPER.writeValueAsString(Map.of("response", "pong", "stat", "FAIL"));
        val service = new BasicDuoSecurityAuthenticationService(
            casProperties.getAuthn().getMfa().getDuo().get(0), httpClient);
        try (val webServer = new MockWebServer(6556,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "Output"),
            HttpStatus.OK)) {
            webServer.start();
            assertFalse(service.ping());
        }
        assertFalse(service.ping());
    }

    @Test
    public void verifyPingFailsToParse() throws Exception {
        val results = MAPPER.writeValueAsString(UUID.randomUUID().toString());
        val props = casProperties.getAuthn().getMfa().getDuo().get(0);
        props.setDuoApiHost(null);
        val service = new BasicDuoSecurityAuthenticationService(props, httpClient);
        try (val webServer = new MockWebServer(6556,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "Output"),
            HttpStatus.OK)) {
            webServer.start();
            assertFalse(service.ping());
        }
    }
}
