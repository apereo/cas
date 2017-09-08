package org.apereo.cas.support.events.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.InMemoryCasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasEventsInMemoryRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casEventsMemoryRepositoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEventsInMemoryRepositoryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasEventsInMemoryRepositoryConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000_000;
    private static final long EXPIRATION_TIME = 2;
    
    @Bean
    public CasEventRepository casEventRepository() {
        final LoadingCache<String, CasEvent> storage = Caffeine.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .expireAfterWrite(EXPIRATION_TIME, TimeUnit.HOURS)
                .build(s -> {
                    LOGGER.error("Load operation of the cache is not supported.");
                    return null;
                });
        LOGGER.debug("Created an in-memory event repository to store CAS events for [{}] hours", EXPIRATION_TIME);
        return new InMemoryCasEventRepository(storage);
    }
}
