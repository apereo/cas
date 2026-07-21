package org.apereo.cas.adaptors.duo.web;

import module java.base;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DuoSecurityAdminApiEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTestAutoConfigurations
@Import(DuoSecurityAdminApiEndpointTests.DuoSecurityMultifactorTestConfiguration.class)
@SpringBootTest(classes = CasCoreWebAutoConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-admin-secret-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-admin-integration-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-secret-key=cGKL1OndjtknbmVOWaFmisaghiNFEKXHxgXCJEBr",
        "cas.authn.mfa.duo[0].duo-integration-key=DIZXVRQD3OMZ6XXMNFQ9",
        "cas.authn.mfa.duo[0].duo-api-host=localhost:8443",
        "cas.http-client.host-name-verifier=none",
        "management.endpoint.duoAdmin.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class DuoSecurityAdminApiEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @TestConfiguration(value = "DuoSecurityMultifactorTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityMultifactorTestConfiguration {
        @Bean
        public DuoSecurityMultifactorAuthenticationProvider duoProvider(
            @Qualifier(TenantExtractor.BEAN_NAME) final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier("noRedirectHttpClient") final HttpClient httpClient) {
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

        @Bean
        public DuoSecurityAdminApiEndpoint duoAdminApiEndpoint(final CasConfigurationProperties casProperties,
                                                               final ConfigurableApplicationContext applicationContext) {
            return new DuoSecurityAdminApiEndpoint(casProperties, applicationContext);
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        try (val webServer = new MockWebServer(8443,
            new ByteArrayResource("{\"stat\": \"OK\" }".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            mockMvc.perform(get("/actuator/duoAdmin/casuser")
                    .param("providerId", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        }

        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            webServer.start();
            mockMvc.perform(get("/actuator/duoAdmin/casuser")
                    .param("providerId", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['%s'].userId".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].firstName".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].lastName".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].email".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].realName".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].lastLogin".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].created".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].devices".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isNotEmpty())
                .andExpect(jsonPath("$['%s'].bypassCodes".formatted(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)).isEmpty());
        }
    }

    @Test
    void verifyCreateBypassCodes() throws Throwable {
        val data = Map.of("stat", "OK", "response", CollectionUtils.wrapList("123456"));
        val entity = MAPPER.writeValueAsString(data);
        try (val webServer = new MockWebServer(8443,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            mockMvc.perform(post("/actuator/duoAdmin/bypassCodes")
                    .param("providerId", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)
                    .param("userId", "mghfytgdq")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
        }
    }

    @Test
    void verifyUserUpdates() throws Throwable {
        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            webServer.start();
            val account = new DuoSecurityUserAccount().setStatus(DuoSecurityUserAccountStatus.AUTH);
            mockMvc.perform(put("/actuator/duoAdmin/casuser")
                    .param("providerId", DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(account))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
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
