package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.JpaTicketEntityFactory;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.integration.IntegrationDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
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
@Configuration(value = "JpaTicketRegistryConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "cas.ticket.registry.jpa", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JpaTicketRegistryConfiguration {

    @Configuration(value = "JpaTicketRegistryDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceTicket")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CloseableDataSource dataSourceTicket(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa());
        }

    }

    @Configuration(value = "JpaTicketRegistryEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryEntityConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> ticketPackagesToScan(final CasConfigurationProperties casProperties) {
            val jpa = casProperties.getTicket().getRegistry().getJpa();
            val type = new JpaTicketEntityFactory(jpa.getDialect()).getType();
            return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName()));
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket,
            @Qualifier("ticketPackagesToScan")
            final BeanContainer<String> ticketPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
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
        public TicketRegistry ticketRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaTicketRegistryTransactionTemplate")
            final TransactionTemplate jpaTicketRegistryTransactionTemplate,
            @Qualifier(TicketCatalog.BEAN_NAME)
            final TicketCatalog ticketCatalog,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            val jpa = casProperties.getTicket().getRegistry().getJpa();
            val bean = new JpaTicketRegistry(jpa.getTicketLockType(), ticketCatalog,
                jpaBeanFactory, jpaTicketRegistryTransactionTemplate, casProperties);
            bean.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa"));
            return bean;
        }
    }

    @Configuration(value = "JpaTicketRegistryLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(prefix = "cas.ticket.registry.core", name = "enable-locking", havingValue = "true", matchIfMissing = true)
    public static class JpaTicketRegistryLockingConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public org.springframework.integration.jdbc.lock.LockRepository jdbcLockRepository(
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket) {
            return new org.springframework.integration.jdbc.lock.DefaultLockRepository(dataSourceTicket);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry jdbcLockRegistry(
            @Qualifier("jdbcLockRepository")
            final org.springframework.integration.jdbc.lock.LockRepository jdbcLockRepository) {
            return new JdbcLockRegistry(jdbcLockRepository);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            @Qualifier("jdbcLockRegistry")
            final LockRegistry jdbcLockRegistry) {
            return new DefaultLockRepository(jdbcLockRegistry);
        }

        @Bean
        IntegrationDataSourceScriptDatabaseInitializer casTicketRegistryLockDataSourceScriptDatabaseInitializer(
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket,
            final IntegrationProperties properties) {
            return new IntegrationDataSourceScriptDatabaseInitializer(dataSourceTicket, properties.getJdbc());
        }
    }
}
