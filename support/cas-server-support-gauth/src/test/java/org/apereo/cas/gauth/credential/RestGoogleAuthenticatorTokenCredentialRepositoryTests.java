package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.gauth.rest.url=http://example.com")
@Getter
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class RestGoogleAuthenticatorTokenCredentialRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(CasGoogleAuthenticator.BEAN_NAME)
    private CasGoogleAuthenticator googleAuthenticatorInstance;

    @Test
    void verifyFailOps() throws Throwable {
        val entity = MAPPER.writeValueAsString(List.of("----"));
        try (val webServer = new MockWebServer(entity)) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);

            webServer.start();
            assertNull(repo.get("casuser", 1));
            assertNull(repo.get(1));
            assertNull(repo.get("casuser"));
            assertEquals(0, repo.count());
            assertEquals(0, repo.count("casuser"));
            assertNull(repo.update(null));
        }
    }

    @Test
    void verifyLoad() throws Throwable {
        try (val webServer = new MockWebServer()) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());
            val entity = MAPPER.writeValueAsString(CollectionUtils.wrapArrayList(account));
            webServer.responseBody(entity);

            webServer.start();
            assertFalse(repo.load().isEmpty());
        }
    }

    @Test
    void verifyDelete() {
        try (val webServer = new MockWebServer("1")) {
            webServer.start();
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);

            assertDoesNotThrow(() -> {
                repo.delete("casuser");
                repo.delete(12345);
                repo.deleteAll();
            });
        }
    }

    @Test
    void verifyGet() throws Throwable {
        try (val webServer = new MockWebServer()) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());
            val entity = MAPPER.writeValueAsString(CollectionUtils.wrapList(account));
            webServer.responseBody(entity);
            webServer.start();
            assertFalse(repo.get(account.getUsername()).isEmpty());
        }
    }

    @Test
    void verifyGetById() throws Throwable {
        try (val webServer = new MockWebServer()) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());
            val entity = MAPPER.writeValueAsString(account);
            webServer.responseBody(entity);

            webServer.start();
            assertNotNull(repo.get(account.getId()));
        }
    }

    @Test
    void verifyGetByIdAndUser() throws Throwable {
        try (val webServer = new MockWebServer()) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());
            val entity = MAPPER.writeValueAsString(account);
            webServer.responseBody(entity);

            webServer.start();
            assertNotNull(repo.get(account.getUsername(), account.getId()));
        }
    }

    @Test
    void verifyCount() {
        try (val webServer = new MockWebServer("1")) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            webServer.start();
            assertEquals(1, repo.count());
        }
    }

    @Test
    void verifyCountByUser() {

        try (val webServer = new MockWebServer("1")) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            webServer.start();
            assertEquals(1, repo.count("casuser"));
        }
    }

    @Test
    void verifySave() throws Throwable {
        try (val webServer = new MockWebServer()) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());
            val entity = MAPPER.writeValueAsString(account);
            webServer.responseBody(entity);
            webServer.start();
            assertDoesNotThrow(() -> {
                val toSave = OneTimeTokenAccount.builder()
                    .username(account.getUsername())
                    .secretKey(account.getSecretKey())
                    .validationCode(0)
                    .scratchCodes(List.of())
                    .name(UUID.randomUUID().toString())
                    .tenant(UUID.randomUUID().toString())
                    .build();
                repo.save(toSave);
            });
        }
    }

    @Test
    void verifySaveFail() {
        try (val webServer = new MockWebServer(HttpStatus.BAD_REQUEST)) {
            val props = new GoogleAuthenticatorMultifactorProperties();
            props.getRest().setUrl("http://localhost:" + webServer.getPort());
            val repo = buildRepositoryInstance(props);
            val account = repo.create(UUID.randomUUID().toString());

            webServer.start();
            assertNull(repo.update(account));
        }
    }

    private OneTimeTokenCredentialRepository buildRepositoryInstance(final GoogleAuthenticatorMultifactorProperties props) {
        return new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString(), CipherExecutor.noOpOfNumberToNumber());
    }

}
