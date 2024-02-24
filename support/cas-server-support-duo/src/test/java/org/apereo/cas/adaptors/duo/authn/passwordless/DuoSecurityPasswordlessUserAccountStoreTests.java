package org.apereo.cas.adaptors.duo.authn.passwordless;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import com.duosecurity.Client;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("DuoSecurity")
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
public class DuoSecurityPasswordlessUserAccountStoreTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    void verifyOperation() throws Throwable {
        val props = casProperties.getAuthn().getMfa().getDuo().getFirst();
        val port = URI.create(props.getDuoApiHost()).getPort();
        try (val webServer = new MockWebServer(true, port, new ClassPathResource("duo-adminapi-user.json"))) {
            webServer.start();
            val user = passwordlessUserAccountStore.findUser("duosecurityuser").orElseThrow();
            assertEquals("duosecurityuser", user.getUsername());
            assertEquals("jsmith@example.com", user.getEmail());
            assertEquals(props.getId(), user.getSource());
        }
    }

    @TestConfiguration
    static class DuoSecurityTestConfiguration {
        @Bean
        public Client duoUniversalPromptAuthenticationClient() throws Exception {
            return mock(Client.class);
        }

    }
}
