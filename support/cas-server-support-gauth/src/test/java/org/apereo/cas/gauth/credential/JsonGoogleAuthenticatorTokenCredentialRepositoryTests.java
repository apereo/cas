package org.apereo.cas.gauth.credential;

import java.nio.file.Files;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountSerializer;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.File;
import java.net.URI;
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
    properties = {
        "cas.authn.mfa.gauth.crypto.enabled=false",
        "cas.authn.mfa.gauth.json.location=file:${java.io.tmpdir}/repository.json"
    })
@Getter
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "registry", mode = ResourceAccessMode.READ_WRITE)
class JsonGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier(CasGoogleAuthenticator.BEAN_NAME)
    private CasGoogleAuthenticator googleAuthenticatorInstance;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyFails() throws Throwable {
        val resource = mock(Resource.class);
        val repo = buildRepositoryInstance(resource);
        assertTrue(repo.load().isEmpty());
        assertNull(repo.update(OneTimeTokenAccount.builder().build()));
        assertEquals(0, repo.count());
        assertDoesNotThrow(() -> repo.delete("casuser"));
        when(resource.getFile()).thenReturn(Files.createTempFile("test", ".json").toFile());
        assertTrue(repo.get("casuser").isEmpty());
    }

    @Test
    void verifyNotExists() {
        val repo = buildRepositoryInstance(new ClassPathResource("acct-bad.json"));
        assertTrue(repo.get("casuser").isEmpty());
    }

    @Test
    void verifyNoAccounts() throws Throwable {
        val file = Files.createTempFile("account", ".json").toFile();
        FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
        val repo = buildRepositoryInstance(new FileSystemResource(file));
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
    void verifyBadResource() throws Throwable {
        val repo = buildRepositoryInstance(new UrlResource(URI.create("http://localhost:8080")));
        assertTrue(repo.get("casuser").isEmpty());
    }

    private OneTimeTokenCredentialRepository buildRepositoryInstance(final Resource resource) {
        return new JsonGoogleAuthenticatorTokenCredentialRepository(resource,
            googleAuthenticatorInstance,
            CipherExecutor.noOpOfStringToString(),
            CipherExecutor.noOpOfNumberToNumber(),
            new OneTimeTokenAccountSerializer(applicationContext));
    }

}
