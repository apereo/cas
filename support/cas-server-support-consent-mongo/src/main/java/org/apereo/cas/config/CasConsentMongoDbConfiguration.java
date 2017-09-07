package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.MongoDbConsentRepository;
import org.apereo.cas.mongo.MongoDbObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link CasConsentMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casConsentMongoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentMongoDbConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ConsentRepository consentRepository() {
        final MongoDbObjectFactory factory = new MongoDbObjectFactory();
        final ConsentProperties.MongoDb mongoProps = casProperties.getConsent().getMongo();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongoProps);
        return new MongoDbConsentRepository(mongoTemplate, mongoProps.getCollection(), mongoProps.isDropCollection());
    }
}
