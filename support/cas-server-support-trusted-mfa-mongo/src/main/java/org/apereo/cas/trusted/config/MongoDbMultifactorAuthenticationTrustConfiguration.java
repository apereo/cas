package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MongoDbMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "mongoDbMultifactorAuthenticationTrustConfiguration", proxyBeanMethods = false)
public class MongoDbMultifactorAuthenticationTrustConfiguration {

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceMfaTrustedAuthnExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    @Autowired
    public MongoTemplate mongoMfaTrustedAuthnTemplate(final CasConfigurationProperties casProperties,
                                                      @Qualifier("sslContext")
                                                      final SSLContext sslContext) {
        val mongo = casProperties.getAuthn().getMfa().getTrusted().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope
    @Bean
    @Autowired
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(final CasConfigurationProperties casProperties,
                                                                @Qualifier("mongoMfaTrustedAuthnTemplate")
                                                                final MongoTemplate mongoMfaTrustedAuthnTemplate,
                                                                @Qualifier("keyGenerationStrategy")
                                                                final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
                                                                @Qualifier("mfaTrustCipherExecutor")
                                                                final CipherExecutor mfaTrustCipherExecutor) {
        return new MongoDbMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(), mfaTrustCipherExecutor, mongoMfaTrustedAuthnTemplate, keyGenerationStrategy);
    }
}
