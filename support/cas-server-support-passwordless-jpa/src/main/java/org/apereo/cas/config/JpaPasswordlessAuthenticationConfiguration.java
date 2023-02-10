package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.impl.token.JpaPasswordlessAuthenticationEntity;
import org.apereo.cas.impl.token.JpaPasswordlessTokenRepository;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.val;
import org.apereo.inspektr.common.Cleanable;
import org.springframework.beans.factory.FactoryBean;
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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Set;

/**
 * This is {@link JpaPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "jpa")
@AutoConfiguration
public class JpaPasswordlessAuthenticationConfiguration {

    @Configuration(value = "JpaPasswordlessAuthenticationEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationEntityConfiguration {
        
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> passwordlessEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaPasswordlessVendorAdapter")
            final JpaVendorAdapter jpaPasswordlessVendorAdapter,
            @Qualifier("passwordlessDataSource")
            final DataSource passwordlessDataSource,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {

            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaPasswordlessVendorAdapter)
                .persistenceUnitName("jpaPasswordlessAuthNContext")
                .dataSource(passwordlessDataSource)
                .packagesToScan(Set.of(JpaPasswordlessAuthenticationEntity.class.getPackage().getName()))
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                casProperties.getAuthn().getPasswordless().getTokens().getJpa());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaPasswordlessVendorAdapter(final CasConfigurationProperties casProperties,
                                                             @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
                                                             final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }
    }
    
    @Configuration(value = "JpaPasswordlessAuthenticationDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "passwordlessDataSource")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource passwordlessDataSource(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getPasswordless().getTokens().getJpa());
        }
    }

    @Configuration(value = "JpaPasswordlessAuthenticationTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationTransactionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager passwordlessTransactionManager(
            @Qualifier("passwordlessEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }
    }
    
    @Configuration(value = "JpaPasswordlessAuthenticationRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationRepositoryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordlessTokenRepository passwordlessTokenRepository(
            @Qualifier("passwordlessCipherExecutor")
            final CipherExecutor passwordlessCipherExecutor,
            final CasConfigurationProperties casProperties) {
            val tokens = casProperties.getAuthn().getPasswordless().getTokens();
            val expiration = Beans.newDuration(tokens.getCore().getExpiration()).toSeconds();
            return new JpaPasswordlessTokenRepository(expiration, passwordlessCipherExecutor);
        }
    }

    @Configuration(value = "JpaPasswordlessAuthenticationCleanerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationCleanerConfiguration {
        
        @ConditionalOnMissingBean(name = "jpaPasswordlessAuthenticationTokenRepositoryCleaner")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable jpaPasswordlessAuthenticationTokenRepositoryCleaner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
            final PasswordlessTokenRepository passwordlessTokenRepository) {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.authn.passwordless.tokens.jpa.cleaner.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> new JpaPasswordlessAuthenticationTokenRepositoryCleaner(passwordlessTokenRepository))
                .otherwiseProxy()
                .get();
        }
    }

    @RequiredArgsConstructor
    public static class JpaPasswordlessAuthenticationTokenRepositoryCleaner implements Cleanable {

        private final PasswordlessTokenRepository repository;

        @Synchronized
        @Override
        @Scheduled(initialDelayString = "${cas.authn.passwordless.tokens.jpa.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.authn.passwordless.tokens.jpa.cleaner.schedule.repeat-interval:PT35S}")
        public void clean() {
            repository.clean();
        }
    }
}
