package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.DefaultCasGoogleAuthenticator;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests {

    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class
    })
    @Nested
    class ScratchCodesEncryptionTests extends BaseOneTimeTokenCredentialRepositoryTests {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(TenantExtractor.BEAN_NAME)
        private TenantExtractor tenantExtractor;


        private final ConcurrentHashMap<String, OneTimeTokenCredentialRepository> repoMap = new ConcurrentHashMap<>();

        @Override
        public OneTimeTokenCredentialRepository getRegistry() {
            return new InMemoryGoogleAuthenticatorTokenCredentialRepository(
                CipherExecutor.noOpOfStringToString(),
                CipherExecutor.noOpOfNumberToNumber(),
                new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor));
        }

        @Override
        public OneTimeTokenCredentialRepository getRegistry(final String testName) {
            return repoMap.computeIfAbsent(testName, name -> this.getRegistry());
        }
    }

    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class
    })
    @Nested
    class DefaultTests extends BaseOneTimeTokenCredentialRepositoryTests {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(TenantExtractor.BEAN_NAME)
        private TenantExtractor tenantExtractor;

        private final ConcurrentHashMap<String, OneTimeTokenCredentialRepository> repoMap = new ConcurrentHashMap<>();

        @Override
        public OneTimeTokenCredentialRepository getRegistry() {
            val crypto = new EncryptionJwtSigningJwtCryptographyProperties();
            crypto.getEncryption().setKeySize(256);
            crypto.setAlg(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            val tokenCredentialCipher = (CipherExecutor) CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class);
            val password = new Base64RandomStringGenerator(16).getNewString();
            val scratchCodesCipher = new JasyptNumberCipherExecutor(password, "scratchCodesCipher");
            return new InMemoryGoogleAuthenticatorTokenCredentialRepository(tokenCredentialCipher, scratchCodesCipher,
                new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor));
        }

        @Override
        public OneTimeTokenCredentialRepository getRegistry(final String testName) {
            return repoMap.computeIfAbsent(testName, name -> this.getRegistry());
        }
    }


    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
        properties = {
            "cas.multitenancy.core.enabled=true",
            "cas.multitenancy.json.location=classpath:/tenants.json"
        })
    @Nested
    @ExtendWith(CasTestExtension.class)
    class MultitenancyTests {

        @Autowired
        @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
        private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

        @Test
        void verifyCreate() throws Throwable {
            val casuser = UUID.randomUUID().toString();
            val account = googleAuthenticatorAccountRegistry.create(casuser);
            account.setTenant("shire");
            val stored = googleAuthenticatorAccountRegistry.save(account);
            assertNotNull(googleAuthenticatorAccountRegistry.get(stored.getId()));
        }
    }
}
