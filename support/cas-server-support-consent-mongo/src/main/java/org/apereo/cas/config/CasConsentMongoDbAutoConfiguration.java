package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.ConsentRepositoryBuilder;
import org.apereo.cas.consent.MongoDbConsentRepository;
import org.apereo.cas.consent.TenantConsentRepositoryBuilder;
import org.apereo.cas.consent.TenantMongoDbConsentRepositoryBuilder;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasConsentMongoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "mongo")
@AutoConfiguration
public class CasConsentMongoDbAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoDbConsentRepositoryBuilder")
    public ConsentRepositoryBuilder mongoDbConsentRepositoryBuilder(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
        return BeanSupplier.of(ConsentRepositoryBuilder.class)
            .when(BeanCondition.on("cas.consent.mongo.collection")
                .given(applicationContext.getEnvironment()))
            .supply(() -> {
                val mongo = casProperties.getConsent().getMongo();
                val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                val mongoTemplate = factory.buildMongoTemplate(mongo);
                MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
                return () -> new MongoDbConsentRepository(mongoTemplate, mongo.getCollection());
            })
            .otherwiseProxy()
            .get();
    }

    @Configuration(value = "CasConsentMongoDbMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentMongoDbMultitenancyConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mongoDbConsentMultitenancyRepositoryBuilder")
        public TenantConsentRepositoryBuilder mongoDbConsentMultitenancyRepositoryBuilder(
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
            return new TenantMongoDbConsentRepositoryBuilder(casSslContext);
        }
    }
}
