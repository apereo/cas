package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.mongodb.spring.session.JdkMongoSessionConverter;
import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasMongoSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration({DataMongoAutoConfiguration.class, MongoAutoConfiguration.class})
@EnableMongoHttpSession
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "mongo")
@AutoConfiguration
public class CasMongoSessionAutoConfiguration {
    private static final int DURATION_MINUTES = 10;

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter(Duration.ofMinutes(DURATION_MINUTES));
    }
}
