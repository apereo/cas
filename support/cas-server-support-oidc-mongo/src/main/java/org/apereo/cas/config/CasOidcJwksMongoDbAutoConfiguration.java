package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.mongo.OidcMongoDbJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link CasOidcJwksMongoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@AutoConfiguration
public class CasOidcJwksMongoDbAutoConfiguration {

    private static final BeanCondition CONDITION_HOST = BeanCondition.on("cas.authn.oidc.jwks.mongo.host");

    private static final BeanCondition CONDITION_COLLECTION = BeanCondition.on("cas.authn.oidc.jwks.mongo.collection");

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "mongoOidcJsonWebKeystoreTemplate")
    public MongoOperations mongoOidcJsonWebKeystoreTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        return BeanSupplier.of(MongoOperations.class)
            .when(CONDITION_HOST.given(applicationContext.getEnvironment()))
            .and(CONDITION_COLLECTION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val mongo = casProperties.getAuthn().getOidc().getJwks().getMongo();
                val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                val mongoTemplate = factory.buildMongoTemplate(mongo);
                MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
                return mongoTemplate;
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public Supplier<OidcJsonWebKeystoreGeneratorService> mongoOidcJsonWebKeystoreGeneratorService(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoOidcJsonWebKeystoreTemplate")
        final MongoOperations mongoOidcJsonWebKeystoreTemplate) {
        return BeanSupplier.of(Supplier.class)
            .when(CONDITION_HOST.given(applicationContext.getEnvironment()))
            .and(CONDITION_COLLECTION.given(applicationContext.getEnvironment()))
            .supply(() -> () -> new OidcMongoDbJsonWebKeystoreGeneratorService(mongoOidcJsonWebKeystoreTemplate,
                casProperties.getAuthn().getOidc()))
            .otherwiseProxy()
            .get();
    }

}
