package org.apereo.cas.support.events.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.InMemoryCasEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasEventsMemoryRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casEventsMemoryRepositoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEventsInMemoryRepositoryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasEventsInMemoryRepositoryConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1000;

    @ConditionalOnMissingBean(name = "casEventRepository")
    @Bean
    public CasEventRepository casEventRepository() {
        final LoadingCache<String, CasEvent> storage = CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, CasEvent>() {
                    @Override
                    public CasEvent load(final String s) throws Exception {
                        LOGGER.error("Load operation of the cache is not supported.");
                        return null;
                    }
                });
        return new InMemoryCasEventRepository(storage);
    }
}
