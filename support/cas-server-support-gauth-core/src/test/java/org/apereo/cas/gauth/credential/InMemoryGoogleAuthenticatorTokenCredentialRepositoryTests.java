package org.apereo.cas.gauth.credential;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;

import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ConcurrentHashMap;


/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
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
