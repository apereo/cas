package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryEncodingTests}.
 *
 * @author Jerome LELEU
 * @since 6.6.0
 */
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreUtilConfiguration.class
})
@Tag("MFAProvider")
public class InMemoryGoogleAuthenticatorTokenCredentialRepositoryEncodingTests extends BaseOneTimeTokenCredentialRepositoryTests {
    private final ConcurrentHashMap<String, OneTimeTokenCredentialRepository> repoMap = new ConcurrentHashMap<>();

    @Override
    public OneTimeTokenCredentialRepository getRegistry() {
        val crypto = new EncryptionJwtSigningJwtCryptographyProperties();
        crypto.getEncryption().setKeySize(256);
        val tokenCredentialCipher = (CipherExecutor) CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class);
        val password = new Base64RandomStringGenerator(16).getNewString();
        val scratchCodesCipher = new JasyptNumberCipherExecutor(password, "scratchCodesCipher");
        return new InMemoryGoogleAuthenticatorTokenCredentialRepository(tokenCredentialCipher, scratchCodesCipher, getGoogle());
    }

    @Override
    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return repoMap.computeIfAbsent(testName, name -> this.getRegistry());
    }
}
