package org.apereo.cas.trusted.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MongoDbMultifactorAuthenticationTrustStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbMultifactorAuthenticationTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbMultifactorAuthenticationTrustConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private CipherExecutor mfaTrustCipherExecutor;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceMfaTrustedAuthnExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
    
    @RefreshScope
    @Bean
    public MongoTemplate mongoMfaTrustedAuthnTemplate() {
        final TrustedDevicesMultifactorProperties.MongoDb mongo = casProperties.getAuthn().getMfa().getTrusted().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();

        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        final TrustedDevicesMultifactorProperties.MongoDb mongodb = casProperties.getAuthn().getMfa().getTrusted().getMongo();
        final MongoDbMultifactorAuthenticationTrustStorage m = 
                new MongoDbMultifactorAuthenticationTrustStorage(
                        mongodb.getCollection(),
                        mongoMfaTrustedAuthnTemplate());
        m.setCipherExecutor(this.mfaTrustCipherExecutor);
        return m;
    }
}
