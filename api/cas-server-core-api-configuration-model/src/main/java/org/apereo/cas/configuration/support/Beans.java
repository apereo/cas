package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.core.cache.ExpiringSimpleCacheProperties;
import org.apereo.cas.configuration.model.core.cache.SimpleCacheProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.util.StringUtils;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


/**
 * A reusable collection of utility methods for object instantiations and configurations used cross various
 * {@code @Bean} creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@UtilityClass
@Slf4j
public class Beans {

    /**
     * New thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static FactoryBean<ExecutorService> newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        val bean = new ThreadPoolExecutorFactoryBean();
        bean.setMaxPoolSize(config.getMaxSize());
        bean.setCorePoolSize(config.getMinSize());
        bean.afterPropertiesSet();
        return bean;
    }


    /**
     * New duration. If the provided length is duration,
     * it will be parsed accordingly, or if it's a numeric value
     * it will be pared as a duration assuming it's provided as seconds.
     *
     * @param value the length in seconds.
     * @return the duration
     */
    public static Duration newDuration(final String value) {
        if (isNeverDurable(value)) {
            return Duration.ZERO;
        }
        if (isInfinitelyDurable(value)) {
            return Duration.ofDays(Integer.MAX_VALUE);
        }
        if (NumberUtils.isCreatable(value)) {
            return Duration.ofSeconds(Long.parseLong(value));
        }
        return Duration.parse(value);
    }

    /**
     * Is infinitely durable?
     *
     * @param value the value
     * @return true/false
     */
    public static boolean isInfinitelyDurable(final String value) {
        return "-1".equalsIgnoreCase(value) || !StringUtils.hasText(value) || "INFINITE".equalsIgnoreCase(value);
    }

    /**
     * Is never durable?
     *
     * @param value the value
     * @return true/false
     */
    public static boolean isNeverDurable(final String value) {
        return "0".equalsIgnoreCase(value) || "NEVER".equalsIgnoreCase(value) || !StringUtils.hasText(value);
    }

    /**
     * Gets temp file path.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the temp file path
     */
    public static String getTempFilePath(final String prefix, final String suffix) {
        return Unchecked.supplier(() -> Files.createTempFile(prefix, suffix).toFile().getCanonicalPath()).get();
    }

    /**
     * New cache.
     *
     * @param <T>         the type parameter
     * @param <V>         the type parameter
     * @param cache       the cache
     * @param expiryAfter the expiry after
     * @return the cache
     */
    public static <T, V> Cache<T, V> newCache(final SimpleCacheProperties cache,
                                              final Expiry<T, V> expiryAfter) {
        return newCacheBuilder(cache)
            .expireAfter(expiryAfter)
            .build();
    }

    /**
     * New cache builder.
     *
     * @param cache the cache
     * @return the caffeine
     */
    public static Caffeine newCacheBuilder(final SimpleCacheProperties cache) {
        val builder = Caffeine.newBuilder()
            .initialCapacity(cache.getInitialCapacity())
            .maximumSize(cache.getCacheSize());
        if (cache instanceof final ExpiringSimpleCacheProperties expiring) {
            builder.expireAfterWrite(newDuration(expiring.getDuration()));
        }
        builder.removalListener((key, value, cause) -> {
            LOGGER.trace("Removing cached value [{}] linked to cache key [{}]; removal cause is [{}]", value, key, cause);
            Unchecked.consumer(__ -> {
                if (value instanceof final AutoCloseable closeable) {
                    Objects.requireNonNull(closeable).close();
                }
            }).accept(value);
        });
        return builder;
    }
}
