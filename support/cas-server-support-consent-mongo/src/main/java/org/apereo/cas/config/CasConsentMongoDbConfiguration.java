package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.MongoDbConsentRepository;
import org.apereo.cas.mongo.MongoDbConnectionFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasConsentMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casConsentMongoDbConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentMongoDbConfiguration {

    @Bean
    @Autowired
    public ConsentRepository consentRepository(final CasConfigurationProperties casProperties,
                                               @Qualifier("sslContext")
                                               final SSLContext sslContext) {
        val mongo = casProperties.getConsent().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return new MongoDbConsentRepository(mongoTemplate, mongo.getCollection());
    }
}
