package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.impl.account.MongoDbPasswordlessUserAccountStore;
import org.apereo.cas.impl.token.MongoDbPasswordlessTokenRepository;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.val;
import org.apereo.inspektr.common.Cleanable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link MongoDbPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "mongo")
@AutoConfiguration
public class MongoDbPasswordlessAuthenticationConfiguration {
    @Configuration(value = "MongoDbPasswordlessAuthenticationAccountsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MongoDbPasswordlessAuthenticationAccountsConfiguration {

        @ConditionalOnMissingBean(name = "mongoDbPasswordlessAuthenticationTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MongoOperations mongoDbPasswordlessAuthenticationTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            val mongo = casProperties.getAuthn().getPasswordless().getAccounts().getMongo();
            val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
            val mongoTemplate = factory.buildMongoTemplate(mongo);
            MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
            return mongoTemplate;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordlessUserAccountStore passwordlessUserAccountStore(
            @Qualifier("mongoDbPasswordlessAuthenticationTemplate")
            final MongoOperations mongoDbPasswordlessAuthenticationTemplate,
            final CasConfigurationProperties casProperties) {
            val accounts = casProperties.getAuthn().getPasswordless().getAccounts();
            return new MongoDbPasswordlessUserAccountStore(mongoDbPasswordlessAuthenticationTemplate, accounts.getMongo());
        }
    }
    @Configuration(value = "MongoDbPasswordlessAuthenticationRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MongoDbPasswordlessAuthenticationRepositoryConfiguration {

        @ConditionalOnMissingBean(name = "mongoDbPasswordlessAuthenticationTokensTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MongoOperations mongoDbPasswordlessAuthenticationTokensTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            val mongo = casProperties.getAuthn().getPasswordless().getTokens().getMongo();
            val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
            val mongoTemplate = factory.buildMongoTemplate(mongo);
            MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
            return mongoTemplate;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordlessTokenRepository passwordlessTokenRepository(
            @Qualifier("mongoDbPasswordlessAuthenticationTokensTemplate")
            final MongoOperations mongoDbPasswordlessAuthenticationTokensTemplate,
            @Qualifier("passwordlessCipherExecutor")
            final CipherExecutor passwordlessCipherExecutor,
            final CasConfigurationProperties casProperties) {
            val tokens = casProperties.getAuthn().getPasswordless().getTokens();
            val expiration = Beans.newDuration(tokens.getCore().getExpiration()).toSeconds();
            return new MongoDbPasswordlessTokenRepository(tokens.getMongo(), expiration,
                passwordlessCipherExecutor, mongoDbPasswordlessAuthenticationTokensTemplate);
        }
    }

    @Configuration(value = "MongoDbPasswordlessAuthenticationCleanerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class MongoDbPasswordlessAuthenticationCleanerConfiguration {

        @ConditionalOnMissingBean(name = "mongoPasswordlessAuthenticationTokenRepositoryCleaner")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable mongoPasswordlessAuthenticationTokenRepositoryCleaner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
            final PasswordlessTokenRepository passwordlessTokenRepository) {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.authn.passwordless.tokens.mongo.cleaner.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> new MongoDbPasswordlessAuthenticationTokenRepositoryCleaner(passwordlessTokenRepository))
                .otherwiseProxy()
                .get();
        }
    }

    @RequiredArgsConstructor
    public static class MongoDbPasswordlessAuthenticationTokenRepositoryCleaner implements Cleanable {

        private final PasswordlessTokenRepository repository;

        @Synchronized
        @Override
        @Scheduled(initialDelayString = "${cas.authn.passwordless.tokens.mongo.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.authn.passwordless.tokens.mongo.cleaner.schedule.repeat-interval:PT35S}")
        public void clean() {
            repository.clean();
        }
    }
}
