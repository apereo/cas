package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDuoSecurityAdminApiServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class, CasCoreHttpConfiguration.class},
    properties = "cas.http-client.host-name-verifier=none")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class DefaultDuoSecurityAdminApiServiceTests {
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
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
        val service = new DefaultDuoSecurityAdminApiService(this.httpClient, props);
        try (val webServer = new MockWebServer(8443)) {
            webServer.responseBodySupplier(() -> new ClassPathResource("duoAdminApiResponse-bypassCodes.json"));
            webServer.start();
            val result = service.getDuoSecurityBypassCodesFor("DU3RP9I2WOC59VZX672N");
            assertFalse(result.isEmpty());
        }
    }
}
