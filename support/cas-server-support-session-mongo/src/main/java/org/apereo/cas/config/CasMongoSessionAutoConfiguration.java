package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import java.time.Duration;

/**
 * This is {@link CasMongoSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableMongoHttpSession
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "mongo")
@AutoConfiguration
public class CasMongoSessionAutoConfiguration {
    private static final int DURATION_MINUTES = 15;

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter(Duration.ofMinutes(DURATION_MINUTES));
    }

    @Bean
    @Primary
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MongoTemplate mongoTemplate(final MongoDatabaseFactory factory,
                                       final MongoConverter converter) {
        return new MongoTemplate(factory, converter);
    }
}
