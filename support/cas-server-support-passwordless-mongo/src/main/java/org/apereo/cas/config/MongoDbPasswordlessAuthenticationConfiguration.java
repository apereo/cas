package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.account.MongoDbPasswordlessUserAccountStore;
import org.apereo.cas.mongo.MongoDbConnectionFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "mongoDbPasswordlessAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbPasswordlessAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "mongoDbPasswordlessAuthenticationTemplate")
    @Bean
    @RefreshScope
    public MongoTemplate mongoDbPasswordlessAuthenticationTemplate() {
        val mongo = casProperties.getAuthn().getPasswordless().getAccounts().getMongo();
        val factory = new MongoDbConnectionFactory();
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    @RefreshScope
    public PasswordlessUserAccountStore passwordlessUserAccountStore() {
        val accounts = casProperties.getAuthn().getPasswordless().getAccounts();
        return new MongoDbPasswordlessUserAccountStore(mongoDbPasswordlessAuthenticationTemplate(), accounts.getMongo());
    }
}
