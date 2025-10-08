package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.DynamoDbHealthIndicator;
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
 * This is {@link CasDynamoDbCoreAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core, module = "dynamodb")
@AutoConfiguration
public class CasDynamoDbCoreAutoConfiguration {
    @ConditionalOnMissingBean(name = "dynamoDbHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("dynamoDbHealthIndicator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HealthIndicator dynamoDbHealthIndicator(final ConfigurableApplicationContext applicationContext) {
        return new DynamoDbHealthIndicator(applicationContext);
    }
}
