package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = "cas.authn.passwordless.tokens.rest.url=https://localhost/tokens")
class RestfulPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier("passwordlessCipherExecutor")
    private CipherExecutor<Serializable, String> passwordlessCipherExecutor;

    @Test
    void verifyRepositoryIsResolvedFromConfiguration() {
        assertInstanceOf(RestfulPasswordlessTokenRepository.class, passwordlessTokenRepository);
    }

    @Test
    void verifyFindToken() {
        val token = createToken("casuser");
        try (val webServer = new MockWebServer(
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val passwordless = repositoryFor(webServer);
            webServer.responseBody(passwordless.encodeToken(token));
            val foundToken = passwordless.findToken("casuser");
            assertNotNull(foundToken);
            assertTrue(foundToken.isPresent());
        }
    }

    @Test
    void verifyFindTokenFails() {
        try (val webServer = new MockWebServer(
            new ByteArrayResource("token".getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val foundToken = repositoryFor(webServer).findToken("casuser");
            assertTrue(foundToken.isEmpty());
        }
    }

    @Test
    void verifySaveToken() {
        val data = "THE_TOKEN";
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val uid = UUID.randomUUID().toString();
            val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
            val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
            val passwordless = repositoryFor(webServer);
            val token = passwordless.createToken(passwordlessUserAccount, passwordlessRequest);
            passwordless.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        }
    }

    @Test
    void verifyDeleteToken() {
        try (val webServer = new MockWebServer(
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val passwordless = repositoryFor(webServer);
            passwordless.deleteToken(PasswordlessAuthenticationToken.builder().token("123456").username("casuser").build());
            passwordless.deleteTokens("casuser");
        }
    }

    @Test
    void verifyClean() {
        try (val webServer = new MockWebServer(
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            repositoryFor(webServer).clean();
        }
    }

    private PasswordlessAuthenticationToken createToken(final String uid) {
        return passwordlessTokenRepository.createToken(
            PasswordlessUserAccount.builder().username(uid).build(),
            PasswordlessAuthenticationRequest.builder().username(uid).build());
    }

    private RestfulPasswordlessTokenRepository repositoryFor(final MockWebServer webServer) {
        val tokens = new CasConfigurationProperties().getAuthn().getPasswordless().getTokens();
        tokens.getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
        return new RestfulPasswordlessTokenRepository(5, tokens.getRest(), passwordlessCipherExecutor);
    }
}
