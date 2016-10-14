package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.apereo.cas.adaptors.gauth.MongoDbGoogleAuthenticatorAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthentiacatorMongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthentiacatorMongoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class GoogleAuthentiacatorMongoDbConfiguration {

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
            return new SimpleMongoDbFactory(new MongoClientURI(
                    casProperties.getAuthn().getMfa().getGauth().getMongodb().getClientUri()));
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }
    
    @Bean
    public ICredentialRepository googleAuthenticatorAccountRegistry() {
        return new MongoDbGoogleAuthenticatorAccountRegistry(
                mongoDbGoogleAuthenticatorTemplate(),
                casProperties.getAuthn().getMfa().getGauth().getMongodb().getCollection(),
                casProperties.getAuthn().getMfa().getGauth().getMongodb().isDropCollection()
        );
    }
}
