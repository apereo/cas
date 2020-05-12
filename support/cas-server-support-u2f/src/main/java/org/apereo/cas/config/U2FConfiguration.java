package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.U2FAuthenticationRegistrationRecordCipherExecutor;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FGroovyResourceDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJsonResourceDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FRestResourceDeviceRepository;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.U2F;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link U2FConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "u2fConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class U2FConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "transactionManagerU2f")
    @Bean
    public PlatformTransactionManager transactionManagerU2f() {
        return new PseudoPlatformTransactionManager();
    }

    @ConditionalOnMissingBean(name = "u2fDeviceRepositoryCleanerScheduler")
    @Bean
    @Autowired
    @ConditionalOnProperty(prefix = "authn.mfa.u2f.cleaner", name = "enabled", havingValue = "true", matchIfMissing = true)
    public U2FDeviceRepositoryCleanerScheduler u2fDeviceRepositoryCleanerScheduler(
        @Qualifier("u2fDeviceRepository") final U2FDeviceRepository storage) {
        return new U2FDeviceRepositoryCleanerScheduler(storage);
    }

    @ConditionalOnMissingBean(name = "u2fService")
    @Bean
    public U2F u2fService() {
        return new U2F();
    }

    @ConditionalOnMissingBean(name = "u2fDeviceRepository")
    @Bean
    @RefreshScope
    public U2FDeviceRepository u2fDeviceRepository() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();

        final LoadingCache<String, String> requestStorage =
            Caffeine.newBuilder()
                .expireAfterWrite(u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit())
                .build(key -> StringUtils.EMPTY);

        if (u2f.getJson().getLocation() != null) {
            return new U2FJsonResourceDeviceRepository(requestStorage,
                u2f.getJson().getLocation(),
                u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit());
        }

        if (u2f.getGroovy().getLocation() != null) {
            return new U2FGroovyResourceDeviceRepository(requestStorage,
                u2f.getGroovy().getLocation(),
                u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit());
        }

        if (StringUtils.isNotBlank(u2f.getRest().getUrl())) {
            return new U2FRestResourceDeviceRepository(requestStorage,
                u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit(), u2f.getRest());
        }

        final LoadingCache<String, Map<String, String>> userStorage =
            Caffeine.newBuilder()
                .expireAfterWrite(u2f.getExpireDevices(), u2f.getExpireDevicesTimeUnit())
                .build(key -> new HashMap<>(0));
        return new U2FInMemoryDeviceRepository(userStorage, requestStorage);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fRegistrationRecordCipherExecutor")
    public CipherExecutor u2fRegistrationRecordCipherExecutor() {
        val crypto = casProperties.getAuthn().getMfa().getU2f().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, U2FAuthenticationRegistrationRecordCipherExecutor.class);
        }
        LOGGER.info("U2F registration record encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "U2F registration records for MFA");
        return CipherExecutor.noOp();
    }

    /**
     * The device cleaner scheduler.
     */
    @RequiredArgsConstructor
    public static class U2FDeviceRepositoryCleanerScheduler {
        private final U2FDeviceRepository repository;

        @Scheduled(initialDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.start-delay:PT20S}",
            fixedDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.repeat-interval:PT15M}")
        public void run() {
            LOGGER.debug("Starting to clean expired U2F devices from repository");
            this.repository.clean();
        }
    }
}
