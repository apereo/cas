package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.duosecurity.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreWebAutoConfiguration.class,
    properties = "cas.http-client.host-name-verifier=none")
@EnableConfigurationProperties({
    CasConfigurationProperties.class,
    WebProperties.class
})
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
    private HttpClient httpClient;

    @Test
    void verifyUserAccountStatus500Error() throws Exception {
        val payload = Map.of(
            DuoSecurityAuthenticationService.RESULT_KEY_CODE, DuoSecurityAuthenticationService.RESULT_CODE_ERROR_THRESHOLD + 1,
            DuoSecurityAuthenticationService.RESULT_KEY_STAT, "FAIL");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setAccountStatusEnabled(true);
            val username = UUID.randomUUID().toString();
            val result = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.UNAVAILABLE, result.getStatus());
        }
    }

    @Test
    void verifyUserAccountStatusUnknown() throws Exception {
        val payload = Map.of(
            DuoSecurityAuthenticationService.RESULT_KEY_CODE, 1984,
            DuoSecurityAuthenticationService.RESULT_KEY_STAT, "FAIL");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setAccountStatusEnabled(true);
            val username = UUID.randomUUID().toString();
            val result = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.AUTH, result.getStatus());
        }
    }

    @Test
    void verifyUserAccountStatusWithoutStat() throws Exception {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(
                DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "allow",
                DuoSecurityAuthenticationService.RESULT_KEY_STATUS_MESSAGE, "the message"));
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setAccountStatusEnabled(true);
            val username = UUID.randomUUID().toString();
            val result = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.UNAVAILABLE, result.getStatus());
        }
    }

    @Test
    void verifyUserAccountStatus() throws Exception {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(
                DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "allow",
                DuoSecurityAuthenticationService.RESULT_KEY_STATUS_MESSAGE, "the message"),
            DuoSecurityAuthenticationService.RESULT_KEY_STAT, "OK");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setAccountStatusEnabled(true);
            val username = UUID.randomUUID().toString();
            var results = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.ALLOW, results.getStatus());

            /*
                Now cached...
             */
            results = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.ALLOW, results.getStatus());
        }
    }

    @Test
    void verifyUserAccountStatusEnrolled() throws Exception {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(
                DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "enroll",
                DuoSecurityAuthenticationService.RESULT_KEY_ENROLL_PORTAL_URL, "https://github.com",
                DuoSecurityAuthenticationService.RESULT_KEY_STATUS_MESSAGE, "the message"),
            DuoSecurityAuthenticationService.RESULT_KEY_STAT, "OK");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setAccountStatusEnabled(true);
            val username = UUID.randomUUID().toString();
            var results = service.getUserAccount(username);
            assertEquals(DuoSecurityUserAccountStatus.ENROLL, results.getStatus());
            assertNotNull(results.getEnrollPortalUrl());
        }
    }

    @Test
    void verifyPassCodeAuthn() throws Exception {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "allow"), DuoSecurityAuthenticationService.RESULT_KEY_STAT, "OK");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            val token = new DuoSecurityPasscodeCredential(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "mfa-duo");
            val results = service.authenticate(token);
            assertTrue(results.isSuccess());
            assertEquals(results.getUsername(), token.getId());
        }
    }

    @Test
    void verifyDirectAuthn() throws Throwable {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "allow"), DuoSecurityAuthenticationService.RESULT_KEY_STAT, "OK");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            val token = new DuoSecurityDirectCredential(RegisteredServiceTestUtils.getPrincipal(), "mfa-duo");
            val results = service.authenticate(token);
            assertTrue(results.isSuccess());
            assertEquals(results.getUsername(), token.getId());
        }
    }

    @Test
    void verifyDirectAuthnUnknownEndpoint() throws Throwable {
        val payload = Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESPONSE,
            Map.of(DuoSecurityAuthenticationService.RESULT_KEY_RESULT, "allow"), DuoSecurityAuthenticationService.RESULT_KEY_STAT, "OK");
        try (val webServer = new MockWebServer(true, MAPPER.writeValueAsString(payload))) {
            webServer.start();
            val service = getAuthenticationService(webServer.getPort());
            service.getProperties().setDuoApiHost("httpbin.org/anything/sample1");
            val token = new DuoSecurityDirectCredential(RegisteredServiceTestUtils.getPrincipal(), "mfa-duo");
            val results = service.authenticate(token);
            assertFalse(results.isSuccess());
        }
    }

    private MockDuoSecurityAuthenticationService getAuthenticationService(final int port) {
        val properties = new DuoSecurityMultifactorAuthenticationProperties()
            .setDuoApiHost("localhost:" + port)
            .setDuoSecretKey("0K4VewoOPTar47vFwdUfg9SvAm8GF6yyyaBWCk61")
            .setDuoIntegrationKey("DICLHRWL1KQK5EUAQP46");
        val userCache = Caffeine.newBuilder().<String, DuoSecurityUserAccount>build();
        return new MockDuoSecurityAuthenticationService(properties, httpClient,
            List.of(MultifactorAuthenticationPrincipalResolver.identical()), userCache, mock(TenantExtractor.class));
    }

    private static class MockDuoSecurityAuthenticationService extends BaseDuoSecurityAuthenticationService {

        MockDuoSecurityAuthenticationService(final DuoSecurityMultifactorAuthenticationProperties properties,
                                             final HttpClient httpClient,
                                             final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver,
                                             final Cache<String, DuoSecurityUserAccount> userAccountCache,
                                             final TenantExtractor tenantExtractor) {
            super(properties, httpClient, tenantExtractor, multifactorAuthenticationPrincipalResolver, userAccountCache);
        }

        @Override
        public boolean ping() {
            return true;
        }

        @Override
        public DuoSecurityClient getDuoClient() {
            val client = mock(DuoSecurityClient.class);
            when(client.getDuoApiHost()).thenReturn(properties.getDuoApiHost());
            when(client.getDuoIntegrationKey()).thenReturn(properties.getDuoIntegrationKey());
            when(client.getDuoSecretKey()).thenReturn(properties.getDuoSecretKey());
            when(client.getInstance()).thenReturn(mock(Client.class));
            return client;
        }

        @Override
        protected DuoSecurityAuthenticationResult authenticateInternal(final Credential credential) {
            return null;
        }
    }
}
