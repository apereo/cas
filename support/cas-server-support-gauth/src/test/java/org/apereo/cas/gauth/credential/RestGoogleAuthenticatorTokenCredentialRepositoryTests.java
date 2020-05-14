package org.apereo.cas.gauth.credential;

import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.gauth.rest.url=http://example.com")
@Getter
@Tag("MFA")
public class RestGoogleAuthenticatorTokenCredentialRepositoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("googleAuthenticatorInstance")
    private IGoogleAuthenticator googleAuthenticatorInstance;

    @Test
    public void verifyLoad() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8551");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        val entity = MAPPER.writeValueAsString(List.of());
        try (val webServer = new MockWebServer(8551,
            new ByteArrayResource(entity.getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertTrue(repo.load().isEmpty());
        }
    }

    @Test
    public void verifyDelete() throws Exception {
        val props = new GoogleAuthenticatorMultifactorProperties();
        props.getRest().setUrl("http://localhost:8550");
        val repo = new RestGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            props, CipherExecutor.noOpOfStringToString());
        try (val webServer = new MockWebServer(8550,
            new ByteArrayResource("1".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() throws Throwable {
                    repo.delete("casuser");
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
        val entity = MAPPER.writeValueAsString(account);
        try (val webServer = new MockWebServer(8552,
            new ByteArrayResource(entity.getBytes(UTF_8), "Results"), OK)) {
            webServer.start();
            assertNotNull(repo.get(account.getUsername()));
        }
    }

    @Test
    public void verifyCount() throws Exception {
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
                public void execute() throws Throwable {
                    repo.save(account.getUsername(), account.getSecretKey(), 0, List.of());
                }
            });
        }
    }

    @Test
    public void verifySaveFail() throws Exception {
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
