package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.passwordless.accounts.rest.url=http://localhost:9291")
@Tag("RestfulApi")
public class RestfulPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    @SneakyThrows
    public void verifyAction() {
        val u = PasswordlessUserAccount.builder()
            .email("casuser@example.org")
            .phone("1234567890")
            .username("casuser")
            .name("casuser")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build();
        val data = MAPPER.writeValueAsString(u);
        try (val webServer = new MockWebServer(9291,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val user = passwordlessUserAccountStore.findUser("casuser");
            assertTrue(user.isPresent());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
