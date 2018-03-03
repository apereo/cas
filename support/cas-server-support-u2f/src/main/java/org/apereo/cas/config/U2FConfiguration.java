package org.apereo.cas.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FGroovyResourceDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJsonResourceDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FRestResourceDeviceRepository;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@Configuration("u2fConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FConfiguration.class);

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

    @ConditionalOnMissingBean(name = "u2fDeviceRepository")
    @Bean
    public U2FDeviceRepository u2fDeviceRepository() {
        final U2FMultifactorProperties u2f = casProperties.getAuthn().getMfa().getU2f();

        final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder()
                        .expireAfterWrite(u2f.getExpireRegistrations(), u2f.getExpireRegistrationsTimeUnit())
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
                        .build(key -> new HashMap<>());
        return new U2FInMemoryDeviceRepository(userStorage, requestStorage);
    }

    /**
     * The device cleaner scheduler.
     */
    public static class U2FDeviceRepositoryCleanerScheduler {
        private final U2FDeviceRepository repository;

        public U2FDeviceRepositoryCleanerScheduler(final U2FDeviceRepository repository) {
            this.repository = repository;
        }

        @Scheduled(initialDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.startDelay:PT20S}",
                fixedDelayString = "${cas.authn.mfa.u2f.cleaner.schedule.repeatInterval:PT15M}")
        public void run() {
            LOGGER.debug("Starting to clean expired U2F devices from repository");
            this.repository.clean();
        }
    }
}
