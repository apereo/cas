package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gauth.credential.MongoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorMongoDbTokenRepository;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasGoogleAuthenticatorMongoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator, module = "mongo")
@AutoConfiguration
public class CasGoogleAuthenticatorMongoDbAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public MongoOperations mongoDbGoogleAuthenticatorTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getTokenCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance")
        final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
        final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbGoogleAuthenticatorTemplate")
        final MongoOperations mongoDbGoogleAuthenticatorTemplate) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new MongoDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, mongoDbGoogleAuthenticatorTemplate, mongo.getCollection(),
            googleAuthenticatorAccountCipherExecutor, googleAuthenticatorScratchCodesCipherExecutor);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbGoogleAuthenticatorTemplate")
        final MongoOperations mongoDbGoogleAuthenticatorTemplate) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new GoogleAuthenticatorMongoDbTokenRepository(mongoDbGoogleAuthenticatorTemplate, mongo.getTokenCollection(),
            casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
    }
}
