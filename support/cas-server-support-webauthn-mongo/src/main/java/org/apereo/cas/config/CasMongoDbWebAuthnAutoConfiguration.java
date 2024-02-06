package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.MongoDbWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link CasMongoDbWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class CasMongoDbWebAuthnAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public MongoOperations mongoWebAuthnTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getMfa().getWebAuthn().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoWebAuthnTemplate")
        final MongoOperations mongoWebAuthnTemplate,
        @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
        final CipherExecutor webAuthnCredentialRegistrationCipherExecutor) {
        return new MongoDbWebAuthnCredentialRepository(mongoWebAuthnTemplate, casProperties, webAuthnCredentialRegistrationCipherExecutor);
    }
}
