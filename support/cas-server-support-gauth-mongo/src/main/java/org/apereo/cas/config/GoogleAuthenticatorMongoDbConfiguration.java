package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.MongoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorMongoDbTokenRepository;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.net.ssl.SSLContext;

/**
 * This is {@link GoogleAuthenticatorMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthenticatorMongoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling
public class GoogleAuthenticatorMongoDbConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    public MongoTemplate mongoDbGoogleAuthenticatorTemplate() {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getTokenCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance") final IGoogleAuthenticator googleAuthenticatorInstance,
                                                                               @Qualifier("googleAuthenticatorAccountCipherExecutor")
                                                                               final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new MongoDbGoogleAuthenticatorTokenCredentialRepository(
            googleAuthenticatorInstance,
            mongoDbGoogleAuthenticatorTemplate(),
            mongo.getCollection(),
            googleAuthenticatorAccountCipherExecutor
        );
    }


    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        val mongo = casProperties.getAuthn().getMfa().getGauth().getMongo();
        return new GoogleAuthenticatorMongoDbTokenRepository(mongoDbGoogleAuthenticatorTemplate(),
            mongo.getTokenCollection(),
            casProperties.getAuthn().getMfa().getGauth().getTimeStepSize());
    }
}
