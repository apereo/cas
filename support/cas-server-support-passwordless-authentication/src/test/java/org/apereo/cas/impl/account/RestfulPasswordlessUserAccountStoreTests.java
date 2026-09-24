package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.passwordless.accounts.rest.url=https://localhost/accounts")
@Tag("RestfulApi")
class RestfulPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        val account = PasswordlessUserAccount.builder()
            .email("casuser@example.org")
            .phone("1234567890")
            .username("casuser")
            .name("casuser")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build();
        val data = MAPPER.writeValueAsString(account);
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val user = storeFor(webServer).findUser(PasswordlessAuthenticationRequest
                .builder()
                .username("casuser")
                .build());
            assertTrue(user.isPresent());
        }
    }

    @Test
    void verifyFailsAction() throws Throwable {
        try (val webServer = new MockWebServer(
            new ByteArrayResource("###".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val user = storeFor(webServer).findUser(
                PasswordlessAuthenticationRequest
                    .builder()
                    .username("casuser")
                    .build());
            assertTrue(user.isEmpty());
        }
    }

    private RestfulPasswordlessUserAccountStore storeFor(final MockWebServer webServer) {
        val properties = new PasswordlessAuthenticationRestAccountsProperties();
        properties.setUrl("http://localhost:%s".formatted(webServer.getPort()));
        return new RestfulPasswordlessUserAccountStore(properties, applicationContext);
    }
}
