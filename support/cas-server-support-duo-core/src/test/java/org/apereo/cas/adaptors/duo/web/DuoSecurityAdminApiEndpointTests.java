package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.authn.BasicDuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAdminApiEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class, CasCoreHttpConfiguration.class},
    properties = "cas.http-client.host-name-verifier=none")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ActuatorEndpoint")
public class DuoSecurityAdminApiEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void setup() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val props = new DuoSecurityMultifactorAuthenticationProperties()
            .setDuoApiHost("localhost:8443")
            .setDuoAdminIntegrationKey(UUID.randomUUID().toString())
            .setDuoAdminSecretKey(UUID.randomUUID().toString());
        val duoService = new BasicDuoSecurityAuthenticationService(props, httpClient,
            List.of(), Caffeine.newBuilder().build());
        val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(bean.getDuoAuthenticationService()).thenReturn(duoService);
        when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bean, "duoProvider");
    }

    @Test
    public void verifyOperation() {
        val endpoint = new DuoSecurityAdminApiEndpoint(casProperties, this.applicationContext);
        try (val webServer = new MockWebServer(8443,
            new ByteArrayResource("{\"stat\": \"OK\" }".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val result = endpoint.getUser("casuser", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            assertTrue(result.isEmpty());
        }

        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            webServer.start();
            val result = endpoint.getUser("casuser", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            assertFalse(result.isEmpty());
            val user = result.get(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            assertNotNull(user.getUserId());
            assertNotNull(user.getFirstName());
            assertNotNull(user.getLastName());
            assertNotNull(user.getEmail());
            assertNotNull(user.getRealName());
            assertNotNull(user.getLastLogin());
            assertNotNull(user.getCreated());
            assertFalse(user.getDevices().isEmpty());
            assertTrue(user.getBypassCodes().isEmpty());
        }
    }

    @Test
    public void verifyCreateBypassCodes() throws Exception {
        val endpoint = new DuoSecurityAdminApiEndpoint(casProperties, this.applicationContext);
        val data = Map.of("stat", "OK", "response", CollectionUtils.wrapList("123456"));
        val entity = MAPPER.writeValueAsString(data);
        try (val webServer = new MockWebServer(8443,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val result = endpoint.createBypassCodes(null, DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, "mghfytgdq");
            assertFalse(result.isEmpty());
        }
    }
}
