package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasJpaWebAuthnAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebAuthn)
@AutoConfiguration
public class CasJpaWebAuthnAutoConfiguration {

    @Configuration(value = "JpaWebAuthnTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaWebAuthnTransactionConfiguration {
        @ConditionalOnMissingBean(name = "webAuthnTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations webAuthnTransactionTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier("transactionManagerWebAuthn")
            final PlatformTransactionManager transactionManagerWebAuthn) {
            val template = new TransactionTemplate(transactionManagerWebAuthn);
            template.setIsolationLevelName(casProperties.getAuthn().getMfa().getWebAuthn().getJpa().getIsolationLevelName());
            template.setPropagationBehaviorName(casProperties.getAuthn().getMfa().getWebAuthn().getJpa().getPropagationBehaviorName());
            return template;
        }
        
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
    static class JpaWebAuthnRepositoryConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public WebAuthnCredentialRepository webAuthnCredentialRepository(
            @Qualifier("webAuthnTransactionTemplate")
            final TransactionOperations webAuthnTransactionTemplate,
            @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
            final CipherExecutor webAuthnCredentialRegistrationCipherExecutor,
            final CasConfigurationProperties casProperties) {
            return new JpaWebAuthnCredentialRepository(casProperties,
                webAuthnCredentialRegistrationCipherExecutor, webAuthnTransactionTemplate);
        }

    }

    @Configuration(value = "JpaWebAuthnEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaWebAuthnEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaWebAuthnVendorAdapter(
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaWebAuthnPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaWebAuthnCredentialRegistration.class.getPackage().getName()));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "webAuthnEntityManagerFactory")
        public FactoryBean<EntityManagerFactory> webAuthnEntityManagerFactory(
            @Qualifier("jpaWebAuthnVendorAdapter")
            final JpaVendorAdapter jpaWebAuthnVendorAdapter,
            @Qualifier("dataSourceWebAuthn")
            final DataSource dataSourceWebAuthn,
            final BeanContainer<String> jpaWebAuthnPackagesToScan,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
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
    static class JpaWebAuthnDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceWebAuthn")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceWebAuthn(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getWebAuthn().getJpa());
        }

    }
}
