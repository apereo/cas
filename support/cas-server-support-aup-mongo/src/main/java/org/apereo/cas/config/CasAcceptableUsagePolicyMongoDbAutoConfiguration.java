package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.MongoDbAcceptableUsagePolicyRepository;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
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
 * This is {@link CasAcceptableUsagePolicyMongoDbAutoConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "mongo")
@AutoConfiguration
public class CasAcceptableUsagePolicyMongoDbAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "mongoAcceptableUsagePolicyTemplate")
    public MongoOperations mongoAcceptableUsagePolicyTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {

        return BeanSupplier.of(MongoOperations.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val mongo = casProperties.getAcceptableUsagePolicy().getMongo();
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
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoAcceptableUsagePolicyTemplate")
        final MongoOperations mongoAcceptableUsagePolicyTemplate,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {

        return BeanSupplier.of(AcceptableUsagePolicyRepository.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> new MongoDbAcceptableUsagePolicyRepository(ticketRegistrySupport,
                casProperties.getAcceptableUsagePolicy(), mongoAcceptableUsagePolicyTemplate))
            .otherwise(AcceptableUsagePolicyRepository::noOp)
            .get();
    }
}
