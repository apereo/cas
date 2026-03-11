package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
import org.apereo.cas.oidc.jwks.register.MongoDbClientJwksRegistrationStore;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link CasOidcMongoDbClientJwksConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Configuration(value = "CasOidcMongoDbClientJwksConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "client-jwks-registration", enabledByDefault = false)
class CasOidcMongoDbClientJwksConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "mongoOidcClientJsonWebKeystoreTemplate")
    public MongoOperations mongoOidcClientJsonWebKeystoreTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getOidc().getJwks().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate,
            getCollectionName(casProperties), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoOidcClientJwksRegistrationStore")
    @Bean
    public ClientJwksRegistrationStore clientJwksRegistrationStore(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoOidcClientJsonWebKeystoreTemplate")
        final MongoOperations mongoOidcClientJsonWebKeystoreTemplate) {
        return new MongoDbClientJwksRegistrationStore(
            mongoOidcClientJsonWebKeystoreTemplate, getCollectionName(casProperties));
    }

    private static String getCollectionName(final CasConfigurationProperties casProperties) {
        val mongo = casProperties.getAuthn().getOidc().getJwks().getMongo();
        return StringUtils.defaultIfBlank(mongo.getCollection(), "CasOidcMongoDb") + "-ClientJwksStore";
    }
}
