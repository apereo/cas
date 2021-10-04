package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.MongoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorMongoDbTokenRepository;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@EnableScheduling
@Configuration(value = "googleAuthenticatorMongoDbConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorMongoDbConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public MongoTemplate mongoDbGoogleAuthenticatorTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getTokenCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance")
        final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor, final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbGoogleAuthenticatorTemplate")
        final MongoTemplate mongoDbGoogleAuthenticatorTemplate) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new MongoDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, mongoDbGoogleAuthenticatorTemplate, mongo.getCollection(),
            googleAuthenticatorAccountCipherExecutor);
    }

    @Bean
    @Autowired
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier("mongoDbGoogleAuthenticatorTemplate")
                                                                           final MongoTemplate mongoDbGoogleAuthenticatorTemplate) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new GoogleAuthenticatorMongoDbTokenRepository(mongoDbGoogleAuthenticatorTemplate, mongo.getTokenCollection(),
            casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
    }
}
