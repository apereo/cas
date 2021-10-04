package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.services.JpaRegisteredServiceEntity;
import org.apereo.cas.services.JpaServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This this {@link JpaServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "jpaServiceRegistryConfiguration", proxyBeanMethods = false)
public class JpaServiceRegistryConfiguration {

    @Configuration(value = "JpaServiceRegistryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaServiceRegistryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jpaServiceRegistryExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlanConfigurer jpaServiceRegistryExecutionPlanConfigurer(
            @Qualifier("jpaServiceRegistry")
            final ServiceRegistry jpaServiceRegistry) {
            return plan -> plan.registerServiceRegistry(jpaServiceRegistry);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaServicePersistenceProviderConfigurer")
        public JpaPersistenceProviderConfigurer jpaServicePersistenceProviderConfigurer() {
            return context -> {
                val entities = CollectionUtils.wrapList(JpaRegisteredServiceEntity.class.getName());
                context.getIncludeEntityClasses().addAll(entities);
            };
        }
    }

    @Configuration(value = "JpaServiceRegistryEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaServiceRegistryEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaServiceVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public PersistenceProvider jpaServicePersistenceProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newPersistenceProvider(casProperties.getServiceRegistry().getJpa());
        }

        @Bean
        @ConditionalOnMissingBean(name = "jpaServicePackagesToScan")
        public BeanContainer<String> jpaServicePackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaRegisteredServiceEntity.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean serviceEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceService")
            final DataSource dataSourceService,
            @Qualifier("jpaServiceVendorAdapter")
            final JpaVendorAdapter jpaServiceVendorAdapter,
            @Qualifier("jpaServicePersistenceProvider")
            final PersistenceProvider jpaServicePersistenceProvider,
            @Qualifier("jpaServicePackagesToScan")
            final BeanContainer<String> jpaServicePackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .dataSource(dataSourceService)
                .persistenceUnitName("jpaServiceRegistryContext")
                .jpaVendorAdapter(jpaServiceVendorAdapter)
                .persistenceProvider(jpaServicePersistenceProvider)
                .packagesToScan(jpaServicePackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getServiceRegistry().getJpa());
        }

    }

    @Configuration(value = "JpaServiceRegistryTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaServiceRegistryTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerServiceReg(
            @Qualifier("serviceEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

        @ConditionalOnMissingBean(name = "jdbcServiceRegistryTransactionTemplate")
        @Bean
        @Autowired
        public TransactionTemplate jdbcServiceRegistryTransactionTemplate(final CasConfigurationProperties casProperties,
                                                                          final ConfigurableApplicationContext applicationContext) {
            val t = new TransactionTemplate(applicationContext.getBean(JpaServiceRegistry.BEAN_NAME_TRANSACTION_MANAGER, PlatformTransactionManager.class));
            t.setIsolationLevelName(casProperties.getServiceRegistry().getJpa().getIsolationLevelName());
            t.setPropagationBehaviorName(casProperties.getServiceRegistry().getJpa().getPropagationBehaviorName());
            return t;
        }
    }

    @Configuration(value = "JpaServiceRegistryDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaServiceRegistryDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceService")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceService(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getServiceRegistry().getJpa());
        }

    }

    @Configuration(value = "JpaServiceRegistryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaServiceRegistryBaseConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaServiceRegistry")
        @Autowired
        public ServiceRegistry jpaServiceRegistry(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
            @Qualifier("jdbcServiceRegistryTransactionTemplate")
            final TransactionTemplate jdbcServiceRegistryTransactionTemplate) {
            return new JpaServiceRegistry(applicationContext,
                Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), jdbcServiceRegistryTransactionTemplate);
        }

    }
}
