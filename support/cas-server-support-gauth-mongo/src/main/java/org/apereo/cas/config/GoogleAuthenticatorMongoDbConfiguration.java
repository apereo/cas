package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.MongoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.adaptors.gauth.MongoDbGoogleAuthenticatorTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    public MongoTemplate mongoDbGoogleAuthenticatorTemplate() {
        return new MongoTemplate(mongoDbGoogleAuthenticatorFactory());
    }

    @RefreshScope
    @Bean
    public MongoDbFactory mongoDbGoogleAuthenticatorFactory() {
        try {
            final MultifactorAuthenticationProperties.GAuth.Mongodb mongo = casProperties.getAuthn().getMfa().getGauth().getMongodb();
            return new SimpleMongoDbFactory(new MongoClientURI(mongo.getClientUri()));
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance") 
                                                                                   final IGoogleAuthenticator googleAuthenticatorInstance) {
        final MultifactorAuthenticationProperties.GAuth.Mongodb mongo = casProperties.getAuthn().getMfa().getGauth().getMongodb();
        return new MongoDbGoogleAuthenticatorTokenCredentialRepository(
                googleAuthenticatorInstance,
                mongoDbGoogleAuthenticatorTemplate(),
                mongo.getCollection(),
                mongo.isDropCollection()
        );
    }

    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        final MultifactorAuthenticationProperties.GAuth.Mongodb mongo = casProperties.getAuthn().getMfa().getGauth().getMongodb();
        return new MongoDbGoogleAuthenticatorTokenRepository(mongoDbGoogleAuthenticatorTemplate(),
                mongo.getTokenCollection(),
                mongo.isDropCollection(),
                casProperties.getAuthn().getMfa().getGauth().getTimeStepSize());
    }
}
