package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustCipherExecutor;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.keys.DefaultMultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JsonMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustRecordExpiry;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleaner;
import org.apereo.cas.trusted.web.MultifactorAuthenticationTrustedDevicesReportEndpoint;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMatchingHostname;
import org.apereo.cas.util.thread.Cleanable;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This is {@link MultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices)
@Configuration(value = "MultifactorAuthnTrustConfiguration", proxyBeanMethods = false)
class MultifactorAuthnTrustConfiguration {

    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Configuration(value = "MultifactorAuthnTrustGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustGeneratorConfiguration {

        @ConditionalOnMissingBean(name = "mfaTrustRecordKeyGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustRecordKeyGenerator mfaTrustRecordKeyGenerator() {
            return new DefaultMultifactorAuthenticationTrustRecordKeyGenerator();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustCoreConfiguration {
        @ConditionalOnMissingBean(name = "mfaTrustDeviceNamingStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy() {
            return MultifactorAuthenticationTrustedDeviceNamingStrategy.random();
        }

        @ConditionalOnMissingBean(name = MultifactorAuthenticationTrustStorage.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustStorage mfaTrustEngine(
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTrustCipherExecutor")
            final CipherExecutor mfaTrustCipherExecutor,
            @Qualifier("mfaTrustRecordKeyGenerator")
            final MultifactorAuthenticationTrustRecordKeyGenerator mfaTrustRecordKeyGenerator) {
            val trusted = casProperties.getAuthn().getMfa().getTrusted();
            val storage = Caffeine.newBuilder().initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE).expireAfter(new MultifactorAuthenticationTrustRecordExpiry()).build(s -> {
                    LOGGER.error("Load operation of the cache is not supported.");
                    return null;
                });
            return FunctionUtils.doIf(trusted.getJson().getLocation() != null, () -> {
                LOGGER.debug("Storing trusted device records inside the JSON resource [{}]", trusted.getJson().getLocation());
                return new JsonMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(),
                    mfaTrustCipherExecutor, trusted.getJson().getLocation(),
                    mfaTrustRecordKeyGenerator);
            }, () -> {
                LOGGER.warn("Storing trusted device records in runtime memory. Changes and records will be lost upon CAS restarts");
                return new InMemoryMultifactorAuthenticationTrustStorage(
                    casProperties.getAuthn().getMfa().getTrusted(),
                    mfaTrustCipherExecutor, storage, mfaTrustRecordKeyGenerator);
            }).get();
        }

        @ConditionalOnMissingBean(name = "transactionManagerMfaAuthnTrust")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerMfaAuthnTrust() {
            return new PseudoTransactionManager();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustCryptoConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaTrustCipherExecutor")
        public CipherExecutor mfaTrustCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getMfa().getTrusted().getCrypto();
            if (crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, MultifactorAuthenticationTrustCipherExecutor.class);
            }
            LOGGER.info("Multifactor trusted authentication record encryption/signing is turned off and "
                + "MAY NOT be safe in a production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "trusted authentication records for MFA");
            return CipherExecutor.noOp();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustSchedulerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustSchedulerConfiguration {
        @ConditionalOnMatchingHostname(name = "cas.authn.mfa.trusted.cleaner.schedule.enabled-on-host")
        @ConditionalOnMissingBean(name = "mfaTrustStorageCleaner")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable mfaTrustStorageCleaner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME) final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.authn.mfa.trusted.cleaner.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> new MultifactorAuthenticationTrustStorageCleaner(mfaTrustEngine))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer casMfaTrustAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("ticketCreationActionResolver") final AuditActionResolver ticketCreationActionResolver,
            @Qualifier("returnValueResourceResolver") final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditResourceResolver(AuditResourceResolvers.TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.TRUSTED_AUTHENTICATION_ACTION_RESOLVER, ticketCreationActionResolver);
            };
        }

    }

    @Configuration(value = "MultifactorAuthnTrustWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MultifactorAuthnTrustWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustedDevicesReportEndpoint mfaTrustedDevicesReportEndpoint(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME) final ObjectProvider<@NonNull MultifactorAuthenticationTrustStorage> mfaTrustEngine) {
            return new MultifactorAuthenticationTrustedDevicesReportEndpoint(casProperties, applicationContext, mfaTrustEngine);
        }
    }
}
