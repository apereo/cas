package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.gauth.json.location=file:${java.io.tmpdir}/repository.json")
@Getter
@Tag("MFA")
public class JsonGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier("googleAuthenticatorInstance")
    private IGoogleAuthenticator googleAuthenticatorInstance;

    @Test
    public void verifyFails() throws Exception {
        val resource = mock(Resource.class);
        val repo = new JsonGoogleAuthenticatorTokenCredentialRepository(resource,
            googleAuthenticatorInstance, CipherExecutor.noOpOfStringToString());
        assertTrue(repo.load().isEmpty());
        assertNull(repo.update(OneTimeTokenAccount.builder().build()));
        assertEquals(0, repo.count());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                repo.delete("casuser");
            }
        });
        when(resource.getFile()).thenReturn(File.createTempFile("test", ".json"));
        assertTrue(repo.get("casuser").isEmpty());
    }

    @Test
    public void verifyNotExists() {
        val repo = new JsonGoogleAuthenticatorTokenCredentialRepository(new ClassPathResource("acct-bad.json"),
            googleAuthenticatorInstance, CipherExecutor.noOpOfStringToString());
        assertTrue(repo.get("casuser").isEmpty());
    }

    @Test
    public void verifyNoAccounts() throws Exception {
        val file = File.createTempFile("account", ".json");
        FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
        val repo = new JsonGoogleAuthenticatorTokenCredentialRepository(new FileSystemResource(file),
            googleAuthenticatorInstance, CipherExecutor.noOpOfStringToString());
        assertTrue(repo.get("casuser").isEmpty());
        repo.deleteAll();
        assertTrue(repo.load().isEmpty());

        val account = repo.create(UUID.randomUUID().toString());
        account.setUsername(null);
        assertNull(repo.save(account));
        account.setUsername(UUID.randomUUID().toString());
        assertNotNull(repo.save(account));
        assertEquals(1, repo.count());
        repo.delete(account.getUsername());
        assertTrue(repo.load().isEmpty());
    }

    @Test
    public void verifyBadResource() throws Exception {
        val repo = new JsonGoogleAuthenticatorTokenCredentialRepository(new UrlResource(new URL("https://httpbin.org/get")),
            googleAuthenticatorInstance, CipherExecutor.noOpOfStringToString());
        assertTrue(repo.get("casuser").isEmpty());
    }
}
