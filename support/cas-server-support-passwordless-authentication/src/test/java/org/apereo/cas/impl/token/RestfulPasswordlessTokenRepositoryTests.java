package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.passwordless.PasswordlessAuthenticationTokensProperties;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = "cas.authn.passwordless.tokens.rest.url=http://localhost:9293")
class RestfulPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier("passwordlessCipherExecutor")
    private CipherExecutor<Serializable, String> passwordlessCipherExecutor;

    private PasswordlessAuthenticationToken createToken(final String uid) {
        return passwordlessTokenRepository.createToken(
            PasswordlessUserAccount.builder().username(uid).build(),
            PasswordlessAuthenticationRequest.builder().username(uid).build());
    }

    @Test
    void verifyFindToken() {
        val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
        tokens.getRest().setUrl("http://localhost:9306");
        val passwordless = getRepository(tokens);

        val token = createToken("casuser");
        val data = passwordless.encodeToken(token);
        try (val webServer = new MockWebServer(9306,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val foundToken = passwordless.findToken("casuser");
            assertNotNull(foundToken);
            assertTrue(foundToken.isPresent());
        }
    }

    @Test
    void verifyFindTokenFails() {
        try (val webServer = new MockWebServer(9306,
            new ByteArrayResource("token".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9306");
            val passwordless = getRepository(tokens);
            val foundToken = passwordless.findToken("casuser");
            assertTrue(foundToken.isEmpty());
        }
    }

    @Test
    void verifySaveToken() {
        val data = "THE_TOKEN";
        try (val webServer = new MockWebServer(9307,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
            tokens.getRest().setUrl("http://localhost:9307");
            val passwordless = getRepository(tokens);

            val uid = UUID.randomUUID().toString();
            val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
            val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
            val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);
            passwordless.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        }
    }

    private RestfulPasswordlessTokenRepository getRepository(final PasswordlessAuthenticationTokensProperties tokens) {
        return new RestfulPasswordlessTokenRepository(5, tokens.getRest(), passwordlessCipherExecutor);
    }

    @Test
    void verifyDeleteToken() {
        try (val webServer = new MockWebServer(9293,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            passwordlessTokenRepository.deleteToken(PasswordlessAuthenticationToken.builder().token("123456").username("casuser").build());
            passwordlessTokenRepository.deleteTokens("casuser");
        }
    }

    @Test
    void verifyClean() {
        try (val webServer = new MockWebServer(9293,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            passwordlessTokenRepository.clean();
        }
    }
}
