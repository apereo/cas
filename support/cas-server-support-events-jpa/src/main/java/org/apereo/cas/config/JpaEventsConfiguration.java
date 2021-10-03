package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.jpa.JpaCasEvent;
import org.apereo.cas.support.events.jpa.JpaCasEventRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link JpaEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "jpaEventsConfiguration", proxyBeanMethods = false)
public class JpaEventsConfiguration {

    @Configuration(value = "JpaEventsDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaEventsDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceEvent")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceEvent(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getEvents().getJpa());
        }

    }

    @Configuration(value = "JpaEventsEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaEventsEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaEventVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }


        @Bean
        public BeanContainer<String> jpaEventPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaCasEvent.class.getPackage().getName()));
        }

        @Lazy
        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaEventVendorAdapter")
            final JpaVendorAdapter jpaEventVendorAdapter,
            @Qualifier("dataSourceEvent")
            final DataSource dataSourceEvent,
            @Qualifier("jpaEventPackagesToScan")
            final BeanContainer<String> jpaEventPackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaEventVendorAdapter)
                .persistenceUnitName("jpaEventRegistryContext")
                .dataSource(dataSourceEvent)
                .packagesToScan(jpaEventPackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getEvents().getJpa());
        }
    }

    @Configuration(value = "JpaEventsTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaEventsTransactionConfiguration {
        @Autowired
        @Bean
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
    public static class JpaEventsRepositoryConfiguration {

        @ConditionalOnMissingBean(name = "jpaEventRepositoryFilter")
        @Bean
        public CasEventRepositoryFilter jpaEventRepositoryFilter() {
            return CasEventRepositoryFilter.noOp();
        }

        @Bean
        @Autowired
        public CasEventRepository casEventRepository(
            @Qualifier("transactionManagerEvents")
            final PlatformTransactionManager transactionManager,
            @Qualifier("jpaEventRepositoryFilter")
            final CasEventRepositoryFilter jpaEventRepositoryFilter) {
            return new JpaCasEventRepository(jpaEventRepositoryFilter, transactionManager);
        }
    }
}
