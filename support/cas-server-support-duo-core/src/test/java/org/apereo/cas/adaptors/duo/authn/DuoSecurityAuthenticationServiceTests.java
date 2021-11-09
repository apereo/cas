package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAuthenticationServiceTests}.
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
@Tag("MFAProvider")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityAuthenticationServiceTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

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
            val duoProperties = casProperties.getAuthn().getMfa().getDuo().get(0);
            duoProperties.setDuoApiHost("http://localhost:6556");
            val service = new BasicDuoSecurityAuthenticationService(
                duoProperties,
                httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()),
                Caffeine.newBuilder().build());
            assertTrue(service.ping());
            assertNotNull(service.getProperties().getDuoApiHost());
        }
    }

    @Test
    public void verifyPingFails() throws Exception {
        val duoProperties = casProperties.getAuthn().getMfa().getDuo().get(0);
        duoProperties.setDuoApiHost("http://localhost:6556");
        val results = MAPPER.writeValueAsString(Map.of("response", "pong", "stat", "FAIL"));
        val service = new BasicDuoSecurityAuthenticationService(
            duoProperties,
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build());
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
        val service = new BasicDuoSecurityAuthenticationService(props,
            httpClient, List.of(MultifactorAuthenticationPrincipalResolver.identical()),
            Caffeine.newBuilder().build());
        try (val webServer = new MockWebServer(6556,
            new ByteArrayResource(results.getBytes(StandardCharsets.UTF_8), "Output"),
            HttpStatus.OK)) {
            webServer.start();
            assertFalse(service.ping());
        }
    }

    @Test
    public void verifyOperation() {
        val service = mock(DuoSecurityAuthenticationService.class);
        when(service.getDuoClient()).thenCallRealMethod();
        when(service.signRequestToken(anyString())).thenCallRealMethod();
        assertTrue(service.getDuoClient().isEmpty());
        assertTrue(service.signRequestToken("anything").isEmpty());
    }
}
