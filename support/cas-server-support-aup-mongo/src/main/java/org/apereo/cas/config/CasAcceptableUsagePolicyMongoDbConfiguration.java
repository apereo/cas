package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.MongoDbAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasAcceptableUsagePolicyMongoDbConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casAcceptableUsagePolicyMongoDbConfiguration", proxyBeanMethods = false)
public class CasAcceptableUsagePolicyMongoDbConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public MongoTemplate mongoAcceptableUsagePolicyTemplate(final CasConfigurationProperties casProperties,
                                                            @Qualifier("sslContext")
                                                            final SSLContext sslContext) {
        val mongo = casProperties.getAcceptableUsagePolicy().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier("mongoAcceptableUsagePolicyTemplate")
                                                                           final MongoTemplate mongoAcceptableUsagePolicyTemplate,
                                                                           @Qualifier("defaultTicketRegistrySupport")
                                                                           final TicketRegistrySupport ticketRegistrySupport) {
        return new MongoDbAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy(), mongoAcceptableUsagePolicyTemplate);
    }
}
