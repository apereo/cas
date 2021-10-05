package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("MFAProvider")
public class InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    private final ConcurrentHashMap<String, OneTimeTokenCredentialRepository> repoMap = new ConcurrentHashMap<>();

    @Override
    public OneTimeTokenCredentialRepository getRegistry() {
        return new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), getGoogle());
    }

    @Override
    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return repoMap.computeIfAbsent(testName, name -> this.getRegistry());
    }
}
