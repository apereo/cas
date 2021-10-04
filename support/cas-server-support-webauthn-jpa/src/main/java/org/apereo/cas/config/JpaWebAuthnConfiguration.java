package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link JpaWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration(value = "JpaWebAuthnConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaWebAuthnConfiguration {

    @Configuration(value = "JpaWebAuthnTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaWebAuthnTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerWebAuthn(
            @Qualifier("webAuthnEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }

    @Configuration(value = "JpaWebAuthnRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaWebAuthnRepositoryConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public WebAuthnCredentialRepository webAuthnCredentialRepository(
            @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
            final CipherExecutor webAuthnCredentialRegistrationCipherExecutor,
            final CasConfigurationProperties casProperties,
            @Qualifier("transactionManagerWebAuthn")
            final PlatformTransactionManager transactionManager) {
            return new JpaWebAuthnCredentialRepository(casProperties,
                webAuthnCredentialRegistrationCipherExecutor, transactionManager);
        }

    }

    @Configuration(value = "JpaWebAuthnEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaWebAuthnEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaWebAuthnVendorAdapter(
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaWebAuthnPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaWebAuthnCredentialRegistration.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "webAuthnEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean webAuthnEntityManagerFactory(
            @Qualifier("jpaWebAuthnVendorAdapter")
            final JpaVendorAdapter jpaWebAuthnVendorAdapter,
            @Qualifier("dataSourceWebAuthn")
            final DataSource dataSourceWebAuthn,
            final BeanContainer<String> jpaWebAuthnPackagesToScan,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .dataSource(dataSourceWebAuthn)
                .packagesToScan(jpaWebAuthnPackagesToScan.toSet())
                .persistenceUnitName("jpaWebAuthnRegistryContext")
                .jpaVendorAdapter(jpaWebAuthnVendorAdapter)
                .build();
            val jpa = casProperties.getAuthn().getMfa().getWebAuthn().getJpa();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, jpa);
        }

    }

    @Configuration(value = "JpaWebAuthnDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaWebAuthnDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceWebAuthn")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceWebAuthn(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getWebAuthn().getJpa());
        }

    }
}
