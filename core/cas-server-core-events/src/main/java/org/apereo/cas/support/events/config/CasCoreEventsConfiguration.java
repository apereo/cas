package org.apereo.cas.support.events.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.support.events.dao.InMemoryCasEventRepository;
import org.apereo.cas.support.events.listener.DefaultCasEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasCoreEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreEventsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreEventsConfiguration.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1000;

    @Autowired
    @Bean
    public DefaultCasEventListener defaultCasEventListener(@Qualifier("casEventRepository") final CasEventRepository casEventRepository) {
        final DefaultCasEventListener l = new DefaultCasEventListener();
        l.setCasEventRepository(casEventRepository);
        return l;
    }

    @ConditionalOnMissingBean(name = "casEventRepository")
    @Bean
    public CasEventRepository casEventRepository() {
        final LoadingCache<String, CasEvent> storage = CacheBuilder.newBuilder()
                .initialCapacity(INITIAL_CACHE_SIZE)
                .maximumSize(MAX_CACHE_SIZE)
                .recordStats()
                .expireAfterWrite(1, TimeUnit.DAYS)
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
