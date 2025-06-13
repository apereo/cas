package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.impl.history.AmnesiacPasswordHistoryService;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryEntity;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
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
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link JdbcPasswordHistoryManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagementHistory, module = "jdbc")
@Configuration(value = "JdbcPasswordHistoryManagementConfiguration", proxyBeanMethods = false)
class JdbcPasswordHistoryManagementConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.history.core.enabled").isTrue();

    @Configuration(value = "JdbcPasswordHistoryManagementEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcPasswordHistoryManagementEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaPasswordHistoryVendorAdapter(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(JpaVendorAdapter.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaPasswordHistoryPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JdbcPasswordHistoryEntity.class.getPackage().getName()));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> passwordHistoryEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaPasswordHistoryVendorAdapter")
            final JpaVendorAdapter jpaPasswordHistoryVendorAdapter,
            @Qualifier("jpaPasswordHistoryPackagesToScan")
            final BeanContainer<String> jpaPasswordHistoryPackagesToScan,
            @Qualifier("jdbcPasswordManagementDataSource")
            final DataSource jdbcPasswordManagementDataSource,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(FactoryBean.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaPasswordHistoryVendorAdapter)
                        .persistenceUnitName("jpaPasswordHistoryContext")
                        .dataSource(jdbcPasswordManagementDataSource)
                        .packagesToScan(jpaPasswordHistoryPackagesToScan.toSet()).build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getAuthn().getPm().getJdbc());
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JdbcPasswordHistoryManagementTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcPasswordHistoryManagementTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerPasswordHistory(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("passwordHistoryEntityManagerFactory")
            final FactoryBean<EntityManagerFactory> emf) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf.getObject());
                    return mgmr;
                }))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "JdbcPasswordHistoryManagementServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcPasswordHistoryManagementServiceConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PasswordHistoryService passwordHistoryService(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(PasswordHistoryService.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(JdbcPasswordHistoryService::new)
                .otherwise(AmnesiacPasswordHistoryService::new)
                .get();
        }
    }
}
