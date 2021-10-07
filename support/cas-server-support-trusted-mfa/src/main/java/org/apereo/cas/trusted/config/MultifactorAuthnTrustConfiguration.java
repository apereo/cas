package org.apereo.cas.trusted.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorCoreProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustCipherExecutor;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.keys.DefaultMultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.keys.LegacyMultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JsonMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleaner;
import org.apereo.cas.trusted.web.MultifactorAuthenticationTrustReportEndpoint;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnMatchingHostname;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreUtilConfiguration.class)
@Slf4j
@Configuration(value = "multifactorAuthnTrustConfiguration", proxyBeanMethods = false)
public class MultifactorAuthnTrustConfiguration {

    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Configuration(value = "MultifactorAuthnTrustGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustGeneratorConfiguration {

        @ConditionalOnMissingBean(name = "mfaTrustRecordKeyGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public MultifactorAuthenticationTrustRecordKeyGenerator mfaTrustRecordKeyGenerator(
            final CasConfigurationProperties casProperties) {
            val type = casProperties.getAuthn().getMfa().getTrusted().getCore().getKeyGeneratorType();
            if (type == TrustedDevicesMultifactorCoreProperties.TrustedDevicesKeyGeneratorTypes.DEFAULT) {
                return new DefaultMultifactorAuthenticationTrustRecordKeyGenerator();
            }
            return new LegacyMultifactorAuthenticationTrustRecordKeyGenerator();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustCoreConfiguration {
        @ConditionalOnMissingBean(name = "mfaTrustDeviceNamingStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy() {
            return MultifactorAuthenticationTrustedDeviceNamingStrategy.random();
        }

        @ConditionalOnMissingBean(name = "mfaTrustEngine")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
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
        public PlatformTransactionManager transactionManagerMfaAuthnTrust() {
            return new PseudoPlatformTransactionManager();
        }
    }

    @Configuration(value = "MultifactorAuthnTrustCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustCryptoConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaTrustCipherExecutor")
        @Autowired
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
    public static class MultifactorAuthnTrustSchedulerConfiguration {
        @ConditionalOnMatchingHostname(name = "cas.authn.mfa.trusted.cleaner.schedule.enabled-on-host")
        @ConditionalOnProperty(prefix = "cas.authn.mfa.trusted.cleaner.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "mfaTrustStorageCleaner")
        @Bean
        public MultifactorAuthenticationTrustStorageCleaner mfaTrustStorageCleaner(
            @Qualifier("mfaTrustEngine")
            final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
            return new MultifactorAuthenticationTrustStorageCleaner(mfaTrustEngine);
        }

    }

    @Configuration(value = "MultifactorAuthnTrustAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustAuditConfiguration {
        @Bean
        public AuditTrailRecordResolutionPlanConfigurer casMfaTrustAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("ticketCreationActionResolver")
            final AuditActionResolver ticketCreationActionResolver,
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditResourceResolver(AuditResourceResolvers.TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.TRUSTED_AUTHENTICATION_ACTION_RESOLVER, ticketCreationActionResolver);
            };
        }

    }

    @Configuration(value = "MultifactorAuthnTrustWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MultifactorAuthnTrustWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public MultifactorAuthenticationTrustReportEndpoint mfaTrustedDevicesReportEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTrustEngine")
            final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
            return new MultifactorAuthenticationTrustReportEndpoint(casProperties, mfaTrustEngine);
        }
    }

    @Slf4j
    private static class MultifactorAuthenticationTrustRecordExpiry implements Expiry<String, MultifactorAuthenticationTrustRecord> {

        @Override
        public long expireAfterCreate(
            @NonNull
            final String key,
            @NonNull
            final MultifactorAuthenticationTrustRecord value, final long currentTime) {
            if (value.getExpirationDate() == null) {
                LOGGER.trace("Multifactor trust record [{}] will never expire", value);
                return Long.MAX_VALUE;
            }
            if (value.isExpired()) {
                LOGGER.trace("Multifactor trust record [{}] is expired", value);
                return 0;
            }
            try {
                val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
                val zonedExp = DateTimeUtils.zonedDateTimeOf(value.getExpirationDate()).truncatedTo(ChronoUnit.SECONDS);
                val nanos = Duration.between(now, zonedExp).toNanos();
                LOGGER.trace("Multifactor trust record [{}] expires in [{}] nanoseconds", value, nanos);
                return nanos;
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            }
            LOGGER.debug("Multifactor trust record [{}] will never expire", value);
            return Long.MAX_VALUE;
        }

        @Override
        public long expireAfterUpdate(
            @NonNull
            final String key,
            @NonNull
            final MultifactorAuthenticationTrustRecord value, final long currentTime,
            @NonNegative
            final long currentDuration) {
            return expireAfterCreate(key, value, currentTime);
        }

        @Override
        public long expireAfterRead(
            @NonNull
            final String key,
            @NonNull
            final MultifactorAuthenticationTrustRecord value, final long currentTime,
            @NonNegative
            final long currentDuration) {
            return expireAfterCreate(key, value, currentTime);
        }
    }
}
