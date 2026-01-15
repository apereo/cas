package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.RedisHealthIndicator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasRedisCoreAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "redis")
public class CasRedisCoreAutoConfiguration {
    @ConditionalOnMissingBean(name = "casRedisHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("redisHealthIndicator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HealthIndicator redisHealthIndicator(final ConfigurableApplicationContext applicationContext) {
        return new RedisHealthIndicator(applicationContext);
    }
}
