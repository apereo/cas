package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.impl.token.JpaPasswordlessTokenRepository;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.jpa.JpaBeanFactory;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Set;

/**
 * This is {@link JpaPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "jpaPasswordlessAuthenticationConfiguration", proxyBeanMethods = false)
public class JpaPasswordlessAuthenticationConfiguration {

    @Configuration(value = "JpaPasswordlessAuthenticationEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationEntityConfiguration {
        private static Set<String> jpaPasswordlessPackagesToScan() {
            return Set.of(PasswordlessAuthenticationToken.class.getPackage().getName());
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean passwordlessEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaPasswordlessVendorAdapter")
            final JpaVendorAdapter jpaPasswordlessVendorAdapter,
            @Qualifier("passwordlessDataSource")
            final DataSource passwordlessDataSource,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {

            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaPasswordlessVendorAdapter)
                .persistenceUnitName("jpaPasswordlessAuthNContext")
                .dataSource(passwordlessDataSource)
                .packagesToScan(jpaPasswordlessPackagesToScan())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getPasswordless().getTokens().getJpa());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaPasswordlessVendorAdapter(final CasConfigurationProperties casProperties,
                                                             @Qualifier("jpaBeanFactory")
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
        @Autowired
        public DataSource passwordlessDataSource(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getPasswordless().getTokens().getJpa());
        }

    }


    @Configuration(value = "JpaPasswordlessAuthenticationTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationTransactionConfiguration {

        @Autowired
        @Bean
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
        @Autowired
        public PasswordlessTokenRepository passwordlessTokenRepository(final CasConfigurationProperties casProperties) {
            val tokens = casProperties.getAuthn()
                .getPasswordless()
                .getTokens();
            return new JpaPasswordlessTokenRepository(tokens.getExpireInSeconds());
        }
    }

    @Configuration(value = "JpaPasswordlessAuthenticationCleanerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaPasswordlessAuthenticationCleanerConfiguration {
        @ConditionalOnProperty(prefix = "cas.authn.passwordless.tokens.jpa.cleaner.schedule", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "jpaPasswordlessAuthenticationTokenRepositoryCleaner")
        @Bean
        @Autowired
        public JpaPasswordlessAuthenticationTokenRepositoryCleaner jpaPasswordlessAuthenticationTokenRepositoryCleaner(
            @Qualifier("passwordlessTokenRepository")
            final PasswordlessTokenRepository passwordlessTokenRepository) {
            return new JpaPasswordlessAuthenticationTokenRepositoryCleaner(passwordlessTokenRepository);
        }

    }

    @RequiredArgsConstructor
    public static class JpaPasswordlessAuthenticationTokenRepositoryCleaner {

        private final PasswordlessTokenRepository repository;

        @Synchronized
        @Scheduled(initialDelayString = "${cas.authn.passwordless.tokens.jpa.cleaner.schedule.start-delay:PT30S}",
            fixedDelayString = "${cas.authn.passwordless.tokens.jpa.cleaner.schedule.repeat-interval:PT35S}")
        public void clean() {
            repository.clean();
        }
    }
}
