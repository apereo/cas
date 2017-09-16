package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.MongoDbAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link CasAcceptableUsagePolicyMongoDbConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casAcceptableUsagePolicyMongoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAcceptableUsagePolicyMongoDbConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public MongoTemplate mongoAcceptableUsagePolicyTemplate() {
        final AcceptableUsagePolicyProperties.MongoDb mongo = casProperties.getAcceptableUsagePolicy().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        final AcceptableUsagePolicyProperties.MongoDb mongo = casProperties.getAcceptableUsagePolicy().getMongo();
        return new MongoDbAcceptableUsagePolicyRepository(ticketRegistrySupport,
                casProperties.getAcceptableUsagePolicy().getAupAttributeName(),
                mongoAcceptableUsagePolicyTemplate(),
                mongo.getCollection());
    }
}
