package org.apereo.cas.adaptors.duo.authn.passwordless;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityClient;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import com.duosecurity.Client;
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
import org.springframework.webflow.execution.Action;
import java.net.URI;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = {
    DuoSecurityPasswordlessUserAccountStoreTests.DuoSecurityTestConfiguration.class,
    BaseDuoSecurityTests.SharedTestConfiguration.class,
    CasPasswordlessAuthenticationAutoConfiguration.class
},
    properties = {
        "cas.http-client.host-name-verifier=none",
        "cas.authn.mfa.duo[0].duo-admin-secret-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-admin-integration-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-secret-key=aGKL0OndjtknbnVOWaFKosiqinNFEKXHxgXCJEBr",
        "cas.authn.mfa.duo[0].duo-integration-key=SIOXVQQD3UMZ8XXMNZQ8",
        "cas.authn.mfa.duo[0].duo-api-host=https://localhost:${random.int[3000,9999]}",
        "cas.authn.mfa.duo[0].passwordless-authentication-enabled=true"
    })
class DuoSecurityPasswordlessUserAccountStoreTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DUO_PASSWORDLESS_VERIFY)
    private Action duoSecurityVerifyPasswordlessAuthenticationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val props = casProperties.getAuthn().getMfa().getDuo().getFirst();
        val port = URI.create(props.getDuoApiHost()).getPort();
        try (val webServer = new MockWebServer(true, port, new ClassPathResource("duo-adminapi-user.json"))) {
            webServer.start();

            val request = PasswordlessAuthenticationRequest.builder().username("casuser").build();
            val user = passwordlessUserAccountStore.findUser(request).orElseThrow();
            assertEquals("jsmith", user.getUsername());
            assertEquals("jsmith@example.com", user.getEmail());
            assertEquals(props.getId(), user.getSource());
            assertNotNull(duoSecurityVerifyPasswordlessAuthenticationAction);

            val data = Map.of("stat", "OK", "response", Map.of("result", "allow", "status", "allow"));
            webServer.responseBodyJson(data);
            val context = MockRequestContext.create(applicationContext);
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, user);
            val result = duoSecurityVerifyPasswordlessAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertNotNull(WebUtils.getAuthentication(context));
            assertNotNull(WebUtils.getCredential(context));
        }
    }

    @TestConfiguration(value = "DuoSecurityTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityTestConfiguration {
        @Bean
        public DuoSecurityClient duoUniversalPromptAuthenticationClient(final CasConfigurationProperties casProperties) {
            val client = mock(DuoSecurityClient.class);
            val duo = casProperties.getAuthn().getMfa().getDuo().getFirst();
            when(client.getDuoApiHost()).thenReturn(duo.getDuoApiHost());
            when(client.getDuoIntegrationKey()).thenReturn(duo.getDuoIntegrationKey());
            when(client.getDuoSecretKey()).thenReturn(duo.getDuoSecretKey());
            when(client.getInstance()).thenReturn(mock(Client.class));
            return client;
        }

    }
}
