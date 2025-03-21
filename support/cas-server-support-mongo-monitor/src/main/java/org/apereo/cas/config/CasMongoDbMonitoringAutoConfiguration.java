package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.CasMongoOperations;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.CompositeHealthIndicator;
import org.apereo.cas.monitor.MongoDbHealthIndicator;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.stream.Collectors;

/**
 * This is {@link CasMongoDbMonitoringAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "mongo")
@AutoConfiguration
public class CasMongoDbMonitoringAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoHealthIndicatorTemplate")
    public BeanContainer<CasMongoOperations> mongoHealthIndicatorTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        return BeanContainer.of(casProperties.getMonitor().getMongo()
            .stream()
            .map(mongoProps -> {
                val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                return factory.buildMongoTemplate(mongoProps);
            })
            .collect(Collectors.toList()));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("mongoHealthIndicator")
    @ConditionalOnMissingBean(name = "mongoHealthIndicator")
    public HealthIndicator mongoHealthIndicator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoHealthIndicatorTemplate")
        final BeanContainer<CasMongoOperations> mongoHealthIndicatorTemplate) {
        val warn = casProperties.getMonitor().getWarn();
        val results = mongoHealthIndicatorTemplate.toList()
            .stream()
            .map(template -> new MongoDbHealthIndicator(template,
                warn.getEvictionThreshold(), warn.getThreshold()))
            .collect(Collectors.toList());
        return new CompositeHealthIndicator(results);
    }
}
