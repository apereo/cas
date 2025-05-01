package org.apereo.cas.adaptors.duo.web;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityClient;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationDeviceManager;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.UniversalPromptDuoSecurityAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAdminApiEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTestAutoConfigurations
@Import(DuoSecurityAdminApiEndpointTests.DuoSecurityMultifactorTestConfiguration.class)
@SpringBootTest(classes =
    CasCoreWebAutoConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-admin-secret-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-admin-integration-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-secret-key=cGKL1OndjtknbmVOWaFmisaghiNFEKXHxgXCJEBr",
        "cas.authn.mfa.duo[0].duo-integration-key=DIZXVRQD3OMZ6XXMNFQ9",
        "cas.authn.mfa.duo[0].duo-api-host=localhost:8443",
        "cas.http-client.host-name-verifier=none"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class DuoSecurityAdminApiEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @TestConfiguration(value = "DuoSecurityMultifactorTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityMultifactorTestConfiguration {
        @Bean
        public DuoSecurityMultifactorAuthenticationProvider duoProvider(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier("noRedirectHttpClient")
            final HttpClient httpClient) {
            val duoService = new UniversalPromptDuoSecurityAuthenticationService(
                casProperties.getAuthn().getMfa().getDuo().getFirst(), httpClient,
                mock(DuoSecurityClient.class), List.of(), Caffeine.newBuilder().build(), tenantExtractor);
            val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(bean.getDuoAuthenticationService()).thenReturn(duoService);
            when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
            when(bean.getDeviceManager()).thenReturn(new DuoSecurityMultifactorAuthenticationDeviceManager(bean));
            return bean;
        }
    }

    @Test
    void verifyOperation() {
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
    void verifyCreateBypassCodes() throws Throwable {
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

    @Test
    void verifyUserUpdates() {
        val endpoint = new DuoSecurityAdminApiEndpoint(casProperties, this.applicationContext);
        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            webServer.start();
            val responseEntity = endpoint.updateUser("casuser", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER,
                new DuoSecurityUserAccount().setStatus(DuoSecurityUserAccountStatus.AUTH));
            assertTrue(responseEntity.hasBody());
            assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        }
    }

    @Test
    void verifyDuoDeviceManager() {
        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            webServer.start();
            val id = casProperties.getAuthn().getMfa().getDuo().getFirst().getId();
            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(id, applicationContext)
                .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
                .orElseThrow();
            
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
            val devices = provider.getDeviceManager().findRegisteredDevices(principal);
            assertEquals(1, devices.size());
            assertTrue(provider.getDeviceManager().hasRegisteredDevices(principal));
            
            provider.getDeviceManager().removeRegisteredDevice(principal, devices.getFirst().getId());
        }
    }
}
