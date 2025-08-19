package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.InMemoryCasEventRepository;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.time.Duration;

/**
 * This is {@link CasEventsInMemoryRepositoryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Events, module = "memory")
@AutoConfiguration
public class CasEventsInMemoryRepositoryAutoConfiguration {

    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 1_000_000;

    private static final long EXPIRATION_TIME = 2;

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepository casEventRepository(
        @Qualifier("casEventRepositoryFilter")
        final CasEventRepositoryFilter casEventRepositoryFilter) {
        val storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .recordStats()
            .expireAfterWrite(Duration.ofHours(EXPIRATION_TIME))
            .<String, CasEvent>build(s -> {
                LOGGER.error("Load operation of the cache is not supported.");
                return null;
            });
        LOGGER.debug("Created an in-memory event repository to store CAS events for [{}] hours", EXPIRATION_TIME);
        return new InMemoryCasEventRepository(casEventRepositoryFilter, storage);
    }

    @ConditionalOnMissingBean(name = "casEventRepositoryFilter")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepositoryFilter casEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }
}
