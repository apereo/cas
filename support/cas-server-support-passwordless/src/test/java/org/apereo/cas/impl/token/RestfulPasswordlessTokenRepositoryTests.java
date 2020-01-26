package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = "cas.authn.passwordless.tokens.rest.url=http://localhost:9293")
public class RestfulPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier("passwordlessCipherExecutor")
    private CipherExecutor<Serializable, String> passwordlessCipherExecutor;

    @Test
    public void verifyFindToken() {
        val token = passwordlessTokenRepository.createToken("casuser");
        val data = passwordlessCipherExecutor.encode(token);
        try (val webServer = new MockWebServer(9306,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9306");
            val passwordless = new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor);

            val foundToken = passwordless.findToken("casuser");
            assertNotNull(foundToken);
            assertTrue(foundToken.isPresent());
        }
    }

    @Test
    public void verifySaveToken() {
        val data = "THE_TOKEN";
        try (val webServer = new MockWebServer(9307,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9307");
            val passwordless = new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor);

            passwordless.saveToken("casuser", data);
        }
    }

    @Test
    public void verifyDeleteToken() {
        try (val webServer = new MockWebServer(9293,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            passwordlessTokenRepository.deleteToken("casuser", "123456");
            passwordlessTokenRepository.deleteTokens("casuser");
        }
    }
}
