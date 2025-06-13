package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.jpa.JpaCasEvent;
import org.apereo.cas.support.events.jpa.JpaCasEventRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasJpaEventsAutoConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Events, module = "jpa")
@AutoConfiguration
@Lazy(false)
public class CasJpaEventsAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.events.jpa.enabled").isTrue().evenIfMissing();

    @Configuration(value = "JpaEventsDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaEventsDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceEvent")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceEvent(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getEvents().getJpa()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaEventsEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaEventsEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaEventVendorAdapter(
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
        public BeanContainer<String> jpaEventPackagesToScan(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> BeanContainer.of(CollectionUtils.wrapSet(JpaCasEvent.class.getPackage().getName())))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> eventsEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaEventVendorAdapter")
            final JpaVendorAdapter jpaEventVendorAdapter,
            @Qualifier("dataSourceEvent")
            final DataSource dataSourceEvent,
            @Qualifier("jpaEventPackagesToScan")
            final BeanContainer<String> jpaEventPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(FactoryBean.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaEventVendorAdapter)
                        .persistenceUnitName("jpaEventRegistryContext")
                        .dataSource(dataSourceEvent)
                        .packagesToScan(jpaEventPackagesToScan.toSet())
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getEvents().getJpa());
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaEventsTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaEventsTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaTransactionManagerEvents")
        public PlatformTransactionManager transactionManagerEvents(
            @Qualifier("eventsEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }
    }

    @Configuration(value = "JpaEventsRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JpaEventsRepositoryConfiguration {

        @ConditionalOnMissingBean(name = "jpaEventRepositoryFilter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventRepositoryFilter jpaEventRepositoryFilter() {
            return CasEventRepositoryFilter.noOp();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasEventRepository casEventRepository(
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasEventRepository.TRANSACTION_MANAGER_EVENTS)
            final PlatformTransactionManager transactionManager,
            @Qualifier("jpaEventRepositoryFilter")
            final CasEventRepositoryFilter jpaEventRepositoryFilter) {
            return new JpaCasEventRepository(jpaEventRepositoryFilter, transactionManager,
                casProperties, jpaBeanFactory);
        }
    }
}
