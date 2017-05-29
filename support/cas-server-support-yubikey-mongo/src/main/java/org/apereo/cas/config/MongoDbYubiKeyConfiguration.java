package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.MongoDbYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
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
 * This is {@link MongoDbYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("mongoDbYubiKeyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbYubiKeyConfiguration {

    @Autowired
    @Qualifier("yubiKeyAccountValidator")
    private YubiKeyAccountValidator yubiKeyAccountValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    public MongoTemplate mongoYubiKeyTemplate() {
        return new MongoTemplate(mongoYubiKeyDbFactory());
    }

    @RefreshScope
    @Bean
    public MongoDbFactory mongoYubiKeyDbFactory() {
        try {
            final MultifactorAuthenticationProperties.YubiKey yubi = casProperties.getAuthn().getMfa().getYubikey();
            return new SimpleMongoDbFactory(new MongoClientURI(yubi.getMongodb().getClientUri()));
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @RefreshScope
    @Bean
    public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
        final MultifactorAuthenticationProperties.YubiKey yubi = casProperties.getAuthn().getMfa().getYubikey();
        return new MongoDbYubiKeyAccountRegistry(yubiKeyAccountValidator,
                mongoYubiKeyTemplate(),
                yubi.getMongodb().getCollection(),
                yubi.getMongodb().isDropCollection());
    }
}
