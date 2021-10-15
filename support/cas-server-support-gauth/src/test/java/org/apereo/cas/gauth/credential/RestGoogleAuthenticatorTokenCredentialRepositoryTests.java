package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.*;
import static org.apereo.cas.util.serialization.JacksonObjectMapperFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

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
public class RestGoogleAuthenticatorTokenCredentialRepositoryTests {
    private static final ObjectMapper MAPPER = builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("googleAuthenticatorInstance")
    private IGoogleAuthenticator googleAuthenticatorInstance;

    @Test
    public void verifyFailOps() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8551");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val entity = MAPPER.writeValueAsString(List.of("----"));
        try (val webServer = new MockWebServer(8551,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
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
    public void verifyLoad() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8551");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        val entity = MAPPER.writeValueAsString(CollectionUtils.wrapArrayList(account));
        try (val webServer = new MockWebServer(8551,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertFalse(repo.load().isEmpty());
        }
    }

    @Test
    public void verifyDelete() {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8550");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        try (val webServer = new MockWebServer(8550,
            new ByteArrayResource("1".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    repo.delete("casuser");
                    repo.delete(12345);
                    repo.deleteAll();
                }
            });
        }
    }

    @Test
    public void verifyGet() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8552");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        val entity = MAPPER.writeValueAsString(CollectionUtils.wrapList(account));
        try (val webServer = new MockWebServer(8552,
            new ByteArrayResource(entity.getBytes(UTF_8), "Results"), OK)) {
            webServer.start();
            assertFalse(repo.get(account.getUsername()).isEmpty());
        }
    }

    @Test
    public void verifyGetById() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8552");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        val entity = MAPPER.writeValueAsString(account);
        try (val webServer = new MockWebServer(8552,
            new ByteArrayResource(entity.getBytes(UTF_8), "Results"), OK)) {
            webServer.start();
            assertNotNull(repo.get(account.getId()));
        }
    }

    @Test
    public void verifyGetByIdAndUser() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8552");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        val entity = MAPPER.writeValueAsString(account);
        try (val webServer = new MockWebServer(8552,
            new ByteArrayResource(entity.getBytes(UTF_8), "Results"), OK)) {
            webServer.start();
            assertNotNull(repo.get(account.getUsername(), account.getId()));
        }
    }

    @Test
    public void verifyCount() {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8552");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        try (val webServer = new MockWebServer(8552,
            new ByteArrayResource("1".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertEquals(1, repo.count());
        }
    }

    @Test
    public void verifyCountByUser() {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8596");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        try (val webServer = new MockWebServer(8596,
            new ByteArrayResource("1".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertEquals(1, repo.count("casuser"));
        }
    }

    @Test
    public void verifySave() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8553");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        val entity = MAPPER.writeValueAsString(account);
        try (val webServer = new MockWebServer(8553,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    val toSave = OneTimeTokenAccount.builder()
                        .username(account.getUsername())
                        .secretKey(account.getSecretKey())
                        .validationCode(0)
                        .scratchCodes(List.of())
                        .name(UUID.randomUUID().toString())
                        .build();
                    repo.save(toSave);
                }
            });
        }
    }

    @Test
    public void verifySaveFail() {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8554");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val account = repo.create(UUID.randomUUID().toString());
        try (val webServer = new MockWebServer(8554,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(UTF_8), "Output"), HttpStatus.BAD_REQUEST)) {
            webServer.start();
            assertNull(repo.update(account));
        }
    }
}
