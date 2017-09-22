package org.apereo.cas.trusted.config;

import com.mongodb.MongoClientURI;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MongoDbMultifactorAuthenticationTrustStorage;
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
        return new MongoTemplate(mongoMfaTrustedAuthnDbFactory());
    }
    
    @RefreshScope
    @Bean
    public MongoDbFactory mongoMfaTrustedAuthnDbFactory() {
        try {
            return new SimpleMongoDbFactory(new MongoClientURI(
                    casProperties.getAuthn().getMfa().getTrusted().getMongodb().getClientUri()));
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        final MongoDbMultifactorAuthenticationTrustStorage m = 
                new MongoDbMultifactorAuthenticationTrustStorage(
                        casProperties.getAuthn().getMfa().getTrusted().getMongodb().getCollection(),
                        casProperties.getAuthn().getMfa().getTrusted().getMongodb().isDropCollection(), 
                        mongoMfaTrustedAuthnTemplate());
        m.setCipherExecutor(this.mfaTrustCipherExecutor);
        return m;
    }
}
