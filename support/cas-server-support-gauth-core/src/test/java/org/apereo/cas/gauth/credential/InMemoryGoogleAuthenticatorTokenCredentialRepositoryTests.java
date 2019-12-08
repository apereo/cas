package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
    MailSenderAutoConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "spring.mail.testConnection=false"
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
