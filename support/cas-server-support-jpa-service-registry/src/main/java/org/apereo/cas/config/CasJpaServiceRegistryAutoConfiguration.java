package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.services.JpaRegisteredServiceEntity;
import org.apereo.cas.services.JpaServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This this {@link CasJpaServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "jpa")
@AutoConfiguration
public class CasJpaServiceRegistryAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.jpa.enabled").isTrue().evenIfMissing();

    @Configuration(value = "JpaServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaServiceRegistryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jpaServiceRegistryExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlanConfigurer jpaServiceRegistryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jpaServiceRegistry") final ServiceRegistry jpaServiceRegistry) {
            return BeanSupplier.of(ServiceRegistryExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerServiceRegistry(jpaServiceRegistry))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaServicePersistenceProviderConfigurer")
        public JpaPersistenceProviderConfigurer jpaServicePersistenceProviderConfigurer(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(JpaPersistenceProviderConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> context -> {
                    val entities = CollectionUtils.wrapList(JpaRegisteredServiceEntity.class.getName());
                    context.getIncludeEntityClasses().addAll(entities);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaServiceRegistryEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaServiceRegistryEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaServiceVendorAdapter(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(JpaVendorAdapter.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PersistenceProvider jpaServicePersistenceProvider(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(PersistenceProvider.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newPersistenceProvider(casProperties.getServiceRegistry().getJpa()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaServicePackagesToScan")
        public BeanContainer<String> jpaServicePackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaRegisteredServiceEntity.class.getPackage().getName()));
        }

        @Bean
        @ConditionalOnMissingBean(name = "serviceEntityManagerFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> serviceEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceService") final DataSource dataSourceService,
            @Qualifier("jpaServiceVendorAdapter") final JpaVendorAdapter jpaServiceVendorAdapter,
            @Qualifier("jpaServicePersistenceProvider") final PersistenceProvider jpaServicePersistenceProvider,
            @Qualifier("jpaServicePackagesToScan") final BeanContainer<String> jpaServicePackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(FactoryBean.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .dataSource(dataSourceService)
                        .persistenceUnitName(JpaServiceRegistry.PERSISTENCE_UNIT_NAME)
                        .jpaVendorAdapter(jpaServiceVendorAdapter)
                        .persistenceProvider(jpaServicePersistenceProvider)
                        .packagesToScan(jpaServicePackagesToScan.toSet())
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getServiceRegistry().getJpa());
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaServiceRegistryTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaServiceRegistryTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerServiceReg(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("serviceEntityManagerFactory") final EntityManagerFactory emf) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf);
                    return mgmr;
                })
                .otherwise(PseudoTransactionManager::new)
                .get();
        }

        @ConditionalOnMissingBean(name = "jdbcServiceRegistryTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations jdbcServiceRegistryTransactionTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier("transactionManagerServiceReg") final PlatformTransactionManager transactionManagerServiceReg,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(TransactionOperations.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val t = new TransactionTemplate(transactionManagerServiceReg);
                    t.setIsolationLevelName(casProperties.getServiceRegistry().getJpa().getIsolationLevelName());
                    t.setPropagationBehaviorName(casProperties.getServiceRegistry().getJpa().getPropagationBehaviorName());
                    return t;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaServiceRegistryDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaServiceRegistryDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getServiceRegistry().getJpa()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaServiceRegistryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaServiceRegistryBaseConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaServiceRegistry")
        public ServiceRegistry jpaServiceRegistry(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
            @Qualifier("jdbcServiceRegistryTransactionTemplate")
            final TransactionOperations jdbcServiceRegistryTransactionTemplate) {
            return BeanSupplier.of(ServiceRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new JpaServiceRegistry(applicationContext,
                    Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new),
                    jdbcServiceRegistryTransactionTemplate))
                .otherwiseProxy()
                .get();
        }
    }
}
