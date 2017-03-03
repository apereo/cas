package org.apereo.cas.trusted.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustCipherExecutor;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorage;
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

import java.io.Serializable;

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
    private static final long MAX_CACHE_SIZE = 1000;

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
        final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage = CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .expireAfterWrite(casProperties.getAuthn().getMfa().getTrusted().getExpiration(),
                        casProperties.getAuthn().getMfa().getTrusted().getTimeUnit())
                .build(new CacheLoader<String, MultifactorAuthenticationTrustRecord>() {
                    @Override
                    public MultifactorAuthenticationTrustRecord load(final String s) throws Exception {
                        LOGGER.error("Load operation of the cache is not supported.");
                        return null;
                    }
                });

        final InMemoryMultifactorAuthenticationTrustStorage m = new InMemoryMultifactorAuthenticationTrustStorage(storage);
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
    public CipherExecutor<Serializable, String> mfaTrustCipherExecutor() {
        if (casProperties.getAuthn().getMfa().getTrusted().isCipherEnabled()) {
            return new MultifactorAuthenticationTrustCipherExecutor(
                    casProperties.getAuthn().getMfa().getTrusted().getEncryptionKey(),
                    casProperties.getAuthn().getMfa().getTrusted().getSigningKey());
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
