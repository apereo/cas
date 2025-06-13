package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustRecordEntityFactory;
import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasJdbcMultifactorAuthnTrustAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "jdbc")
@AutoConfiguration
public class CasJdbcMultifactorAuthnTrustAutoConfiguration {

    @Configuration(value = "JdbcMultifactorAuthnTrustTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcMultifactorAuthnTrustTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager jpaMfaTrustTransactionManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaTrustedAuthnEntityManagerFactory") final EntityManagerFactory emf) {
            val transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(emf);
            return transactionManager;
        }

        @ConditionalOnMissingBean(name = "jpaMfaTrustTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations jpaMfaTrustTransactionTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaMfaTrustTransactionManager")
            final PlatformTransactionManager transactionManagerServiceReg,
            final ConfigurableApplicationContext applicationContext) {
            val transactionTemplate = new TransactionTemplate(transactionManagerServiceReg);
            val jpa = casProperties.getAuthn().getMfa().getTrusted().getJpa();
            transactionTemplate.setIsolationLevelName(jpa.getIsolationLevelName());
            transactionTemplate.setPropagationBehaviorName(jpa.getPropagationBehaviorName());
            return transactionTemplate;
        }
    }

    @Configuration(value = "JdbcMultifactorAuthnTrustEngineConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcMultifactorAuthnTrustEngineConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaMfaTrustEngine")
        public MultifactorAuthenticationTrustStorage mfaTrustEngine(
            @Qualifier("dataSourceMfaTrustedAuthn") final DataSource dataSourceMfaTrustedAuthn,
            @Qualifier("jpaMfaTrustTransactionTemplate")
            final TransactionOperations jpaMfaTrustTransactionTemplate,
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTrustCipherExecutor")
            final CipherExecutor mfaTrustCipherExecutor,
            @Qualifier("mfaTrustRecordKeyGenerator")
            final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
            return new JpaMultifactorAuthenticationTrustStorage(
                casProperties.getAuthn().getMfa().getTrusted(),
                mfaTrustCipherExecutor, keyGenerationStrategy,
                jpaMfaTrustTransactionTemplate, dataSourceMfaTrustedAuthn);
        }
    }

    @Configuration(value = "JdbcMultifactorAuthnTrustEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcMultifactorAuthnTrustEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnVendorAdapter")
        public JpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnPackagesToScan")
        public BeanContainer<String> jpaMfaTrustedAuthnPackagesToScan(final CasConfigurationProperties casProperties) {
            val jpa = casProperties.getAuthn().getMfa().getTrusted().getJpa();
            val type = new JpaMultifactorAuthenticationTrustRecordEntityFactory(jpa.getDialect()).getType();
            return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName()));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> mfaTrustedAuthnEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceMfaTrustedAuthn") final DataSource dataSourceMfaTrustedAuthn,
            @Qualifier("jpaMfaTrustedAuthnPackagesToScan") final BeanContainer<String> jpaMfaTrustedAuthnPackagesToScan,
            @Qualifier("jpaMfaTrustedAuthnVendorAdapter") final JpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder().dataSource(dataSourceMfaTrustedAuthn)
                .packagesToScan(jpaMfaTrustedAuthnPackagesToScan.toSet())
                .persistenceUnitName("jpaMfaTrustedAuthnContext")
                .jpaVendorAdapter(jpaMfaTrustedAuthnVendorAdapter).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                casProperties.getAuthn().getMfa().getTrusted().getJpa());
        }

    }

    @Configuration(value = "JdbcMultifactorAuthnTrustDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcMultifactorAuthnTrustDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceMfaTrustedAuthn")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceMfaTrustedAuthn(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getTrusted().getJpa());
        }

    }
}
