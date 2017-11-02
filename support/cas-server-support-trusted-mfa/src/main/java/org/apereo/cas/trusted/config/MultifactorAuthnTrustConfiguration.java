package org.apereo.cas.trusted.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustCipherExecutor;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.BaseMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JsonMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleaner;
import org.apereo.cas.trusted.web.MultifactorAuthenticationTrustController;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationSetTrustAction;
import org.apereo.cas.trusted.web.flow.MultifactorAuthenticationVerifyTrustAction;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link MultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("multifactorAuthnTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreUtilConfiguration.class)
public class MultifactorAuthnTrustConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthnTrustConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public Action mfaSetTrustAction(@Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage storage) {
        return new MultifactorAuthenticationSetTrustAction(storage, casProperties.getAuthn().getMfa().getTrusted());
    }

    @Bean
    public MultifactorAuthenticationTrustController mfaTrustController(@Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage storage) {
        return new MultifactorAuthenticationTrustController(storage, casProperties.getAuthn().getMfa().getTrusted());
    }

    @Bean
    @RefreshScope
    public Action mfaVerifyTrustAction(@Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage storage) {
        return new MultifactorAuthenticationVerifyTrustAction(storage, casProperties.getAuthn().getMfa().getTrusted());
    }

    @ConditionalOnMissingBean(name = "mfaTrustEngine")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        final TrustedDevicesMultifactorProperties trusted = casProperties.getAuthn().getMfa().getTrusted();
        final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage = Caffeine.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(trusted.getExpiration(), trusted.getTimeUnit())
                .build(s -> {
                    LOGGER.error("Load operation of the cache is not supported.");
                    return null;
                });

        storage.asMap();
        final BaseMultifactorAuthenticationTrustStorage m;
        if (trusted.getJson().getLocation() != null) {
            LOGGER.debug("Storing trusted device records inside the JSON resource [{}]", trusted.getJson().getLocation());
            m = new JsonMultifactorAuthenticationTrustStorage(trusted.getJson().getLocation());
        } else {
            LOGGER.warn("Storing trusted device records in runtime memory. Changes and records will be lost upon CAS restarts");
            m = new InMemoryMultifactorAuthenticationTrustStorage(storage);
        }
        m.setCipherExecutor(mfaTrustCipherExecutor());
        return m;
    }

    @ConditionalOnMissingBean(name = "transactionManagerMfaAuthnTrust")
    @Bean
    public PlatformTransactionManager transactionManagerMfaAuthnTrust() {
        return new PseudoPlatformTransactionManager();
    }

    @Bean
    @RefreshScope
    public CipherExecutor mfaTrustCipherExecutor() {
        final EncryptionJwtSigningJwtCryptographyProperties crypto = casProperties.getAuthn().getMfa().getTrusted().getCrypto();
        if (crypto.isEnabled()) {
            return new MultifactorAuthenticationTrustCipherExecutor(
                    crypto.getEncryption().getKey(),
                    crypto.getSigning().getKey(),
                    crypto.getAlg());
        }
        LOGGER.info("Multifactor trusted authentication record encryption/signing is turned off and "
                + "MAY NOT be safe in a production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "trusted authentication records for MFA");
        return NoOpCipherExecutor.getInstance();
    }

    @ConditionalOnMissingBean(name = "mfaTrustStorageCleaner")
    @Bean
    @Lazy
    public MultifactorAuthenticationTrustStorageCleaner mfaTrustStorageCleaner(
            @Qualifier("mfaTrustEngine") final MultifactorAuthenticationTrustStorage storage) {
        return new MultifactorAuthenticationTrustStorageCleaner(casProperties.getAuthn().getMfa().getTrusted(), storage);
    }
}
