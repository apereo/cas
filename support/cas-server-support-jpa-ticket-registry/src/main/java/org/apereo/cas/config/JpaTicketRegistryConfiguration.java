package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.JpaTicketEntityFactory;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.generic.JpaLockEntity;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategy;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "jpaTicketRegistryConfiguration", proxyBeanMethods = false)
public class JpaTicketRegistryConfiguration {
    
    @Configuration(value = "JpaTicketRegistryDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceTicket")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CloseableDataSource dataSourceTicket(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa());
        }

    }

    @Configuration(value = "JpaTicketRegistryEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryEntityConfiguration {
        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> ticketPackagesToScan(final CasConfigurationProperties casProperties) {
            val jpa = casProperties.getTicket().getRegistry().getJpa();
            val type = new JpaTicketEntityFactory(jpa.getDialect()).getType();
            return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName(),
                JpaLockEntity.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket,
            @Qualifier("ticketPackagesToScan")
            final BeanContainer<String> ticketPackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .persistenceUnitName("jpaTicketRegistryContext")
                .dataSource(dataSourceTicket)
                .packagesToScan(ticketPackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getTicket().getRegistry().getJpa());
        }
    }

    @Configuration(value = "JpaTicketRegistryTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryTransactionConfiguration {
        @Bean
        @Autowired
        public PlatformTransactionManager ticketTransactionManager(
            @Qualifier("ticketEntityManagerFactory")
            final EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
    }

    @Configuration(value = "JpaTicketRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryCoreConfiguration {
        @ConditionalOnMissingBean(name = "jpaTicketRegistryTransactionTemplate")
        @Bean
        @Autowired
        public TransactionTemplate jpaTicketRegistryTransactionTemplate(
            @Qualifier("ticketTransactionManager")
            final PlatformTransactionManager ticketTransactionManager,
            final CasConfigurationProperties casProperties) {
            val template = new TransactionTemplate(ticketTransactionManager);
            val jpa = casProperties.getTicket().getRegistry().getJpa();
            template.setIsolationLevelName(jpa.getIsolationLevelName());
            template.setPropagationBehaviorName(jpa.getPropagationBehaviorName());
            return template;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public TicketRegistry ticketRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaTicketRegistryTransactionTemplate")
            final TransactionTemplate jpaTicketRegistryTransactionTemplate,
            @Qualifier("ticketCatalog")
            final TicketCatalog ticketCatalog,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val jpa = casProperties.getTicket().getRegistry().getJpa();
            val bean = new JpaTicketRegistry(jpa.getTicketLockType(), ticketCatalog,
                jpaBeanFactory, jpaTicketRegistryTransactionTemplate, casProperties);
            bean.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa"));
            return bean;
        }

        @Bean
        @Autowired
        public LockingStrategy lockingStrategy(final CasConfigurationProperties casProperties) {
            val registry = casProperties.getTicket().getRegistry();
            val uniqueId = StringUtils.defaultIfEmpty(casProperties.getHost().getName(), InetAddressUtils.getCasServerHostName());
            return new JpaLockingStrategy("cas-ticket-registry-cleaner",
                uniqueId, Beans.newDuration(registry.getJpa().getJpaLockingTimeout()).getSeconds());
        }
    }
}
