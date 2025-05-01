package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityClient;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import java.net.URI;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationDeviceProviderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SpringBootTest(classes = {
    DuoSecurityMultifactorAuthenticationDeviceProviderActionTests.DuoSecurityTestConfiguration.class,
    DuoSecurityDirectAuthenticationActionTests.DuoMultifactorTestConfiguration.class,
    BaseDuoSecurityTests.SharedTestConfiguration.class
},
    properties = {
        "cas.http-client.host-name-verifier=none",
        "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].duo-api-host=https://localhost:${random.int[3000,9999]}",
        "cas.authn.mfa.duo[0].duo-admin-secret-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-admin-integration-key=SIOXVQQD3UMZ8XXMNZQ8"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
class DuoSecurityMultifactorAuthenticationDeviceProviderActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("duoMultifactorAuthenticationDeviceProviderAction")
    private MultifactorAuthenticationDeviceProviderAction duoMultifactorAuthenticationDeviceProviderAction;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val props = casProperties.getAuthn().getMfa().getDuo().getFirst();
        val port = URI.create(props.getDuoApiHost()).getPort();
        try (val webServer = new MockWebServer(true, port, new ClassPathResource("duo-adminapi-user.json"))) {
            webServer.start();
            duoMultifactorAuthenticationDeviceProviderAction.execute(context);
        }
    }

    @TestConfiguration
    static class DuoSecurityTestConfiguration {
        @Bean
        public DuoSecurityClient duoUniversalPromptAuthenticationClient() {
            return mock(DuoSecurityClient.class);
        }

    }
}
