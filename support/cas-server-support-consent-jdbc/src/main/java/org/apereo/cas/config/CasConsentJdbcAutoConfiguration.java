package org.apereo.cas.config;

import module java.base;
import module java.sql;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentRepositoryBuilder;
import org.apereo.cas.consent.JpaConsentRepository;
import org.apereo.cas.consent.TenantConsentRepositoryBuilder;
import org.apereo.cas.consent.TenantJdbcConsentRepositoryBuilder;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
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
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;

/**
 * This is {@link CasConsentJdbcAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "jdbc")
@AutoConfiguration
public class CasConsentJdbcAutoConfiguration {

    @Configuration(value = "CasConsentJdbcRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcRepositoryConfiguration {

        private static final BeanCondition CONDITION = BeanCondition.on("cas.consent.jpa.enabled").isTrue().evenIfMissing();

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceConsent")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CloseableDataSource dataSourceConsent(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CloseableDataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getConsent().getJpa()))
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaConsentVendorAdapter(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(JpaVendorAdapter.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaConsentPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(ConsentDecision.class.getPackage().getName()));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public EntityManagerFactory consentEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jpaConsentVendorAdapter") final JpaVendorAdapter jpaConsentVendorAdapter,
            @Qualifier("dataSourceConsent") final CloseableDataSource dataSourceConsent,
            @Qualifier("jpaConsentPackagesToScan") final BeanContainer<String> jpaConsentPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) throws Exception {

            return BeanSupplier.of(EntityManagerFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaConsentVendorAdapter)
                        .persistenceUnitName("jpaConsentContext")
                        .dataSource(dataSourceConsent)
                        .packagesToScan(jpaConsentPackagesToScan.toSet())
                        .build();
                    val entityManagerFactory = jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getConsent().getJpa()).getObject();
                    Objects.requireNonNull(entityManagerFactory);
                    return entityManagerFactory;
                }))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jdbcConsentRepositoryBuilder")
        public ConsentRepositoryBuilder jdbcConsentRepositoryBuilder(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceConsent") final CloseableDataSource dataSourceConsent,
            @Qualifier("transactionManagerConsent") final PlatformTransactionManager transactionManagerConsent,
            @Qualifier("consentEntityManagerFactory") final EntityManagerFactory emf) {

            return BeanSupplier.of(ConsentRepositoryBuilder.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> () -> {
                    val entityManager = SharedEntityManagerCreator.createSharedEntityManager(emf);
                    return new JpaConsentRepository(entityManager, dataSourceConsent,
                        new TransactionTemplate(transactionManagerConsent));
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerConsent(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("consentEntityManagerFactory") final EntityManagerFactory emf) {

            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf);
                    return mgmr;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasConsentJdbcMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcMultitenancyConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jdbcConsentMultitenancyRepositoryBuilder")
        public TenantConsentRepositoryBuilder jdbcConsentMultitenancyRepositoryBuilder(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory,
            @Qualifier("jpaConsentPackagesToScan") final BeanContainer<String> jpaConsentPackagesToScan) {

            return BeanSupplier.of(TenantConsentRepositoryBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(() -> new TenantJdbcConsentRepositoryBuilder(jpaConsentPackagesToScan, jpaBeanFactory))
                .otherwise(TenantConsentRepositoryBuilder::noOp)
                .get();
        }
    }
}
