package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDuoSecurityAdminApiServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    DefaultDuoSecurityAdminApiServiceTests.DuoSecurityAdminTestConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].duo-api-host=localhost:${random.int[3000,9999]}",
        "cas.http-client.host-name-verifier=none"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DefaultDuoSecurityAdminApiServiceTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("duoSecurityAdminApiService")
    private DuoSecurityAdminApiService duoSecurityAdminApiService;
    
    @Test
    void verifyCodesAndAccount() throws Throwable {
        val duoProps = casProperties.getAuthn().getMfa().getDuo().getFirst();
        val port = Integer.parseInt(Strings.CI.remove(duoProps.getDuoApiHost(), "localhost:"));

        try (val webServer = new MockWebServer(true, port, new ClassPathResource("duoAdminApiResponse-bypassCodes.json"))) {
            webServer.start();
            val codes = duoSecurityAdminApiService.getDuoSecurityBypassCodesFor("DU3RP9I2WOC59VZX672N");
            assertFalse(codes.isEmpty());

            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-user.json"));
            val userAccount = duoSecurityAdminApiService.modifyDuoSecurityUserAccount(new DuoSecurityUserAccount("casuser"));
            assertFalse(userAccount.isEmpty());
            assertNotNull(userAccount.get().getPhone());
        }
    }
    

    @TestConfiguration(value = "DuoSecurityAdminTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityAdminTestConfiguration {
        @Bean
        public DuoSecurityAdminApiService duoSecurityAdminApiService(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier("noRedirectHttpClient") final HttpClient httpClient,
            final CasConfigurationProperties casProperties) {

            val duoProps = casProperties.getAuthn().getMfa().getDuo().getFirst();
            val service = new DefaultDuoSecurityAdminApiService(httpClient, duoProps);
            val duoService = new UniversalPromptDuoSecurityAuthenticationService(duoProps, httpClient,
                mock(DuoSecurityClient.class), List.of(), Caffeine.newBuilder().build(), tenantExtractor);
            val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(bean.getDuoAuthenticationService()).thenReturn(duoService);
            when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
            return service;
        }
    }
}
