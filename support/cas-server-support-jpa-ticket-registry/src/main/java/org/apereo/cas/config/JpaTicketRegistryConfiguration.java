package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.CasFeatureModule;
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
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
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
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "jpa")
public class JpaTicketRegistryConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.ticket.registry.jpa.enabled").isTrue().evenIfMissing();

    @Configuration(value = "JpaTicketRegistryDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceTicket")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CloseableDataSource dataSourceTicket(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CloseableDataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaTicketRegistryEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryEntityConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> ticketPackagesToScan(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val jpa = casProperties.getTicket().getRegistry().getJpa();
                    val type = new JpaTicketEntityFactory(jpa.getDialect()).getType();
                    return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName()));
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public EntityManagerFactory ticketEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket,
            @Qualifier("ticketPackagesToScan")
            final BeanContainer<String> ticketPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) throws Exception {
            ApplicationContextProvider.holdApplicationContext(applicationContext);
            return BeanSupplier.of(EntityManagerFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                        .persistenceUnitName("jpaTicketRegistryContext")
                        .dataSource(dataSourceTicket)
                        .packagesToScan(ticketPackagesToScan.toSet())
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getTicket().getRegistry().getJpa()).getObject();
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaTicketRegistryTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager ticketTransactionManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("ticketEntityManagerFactory")
            final EntityManagerFactory emf) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new JpaTransactionManager(emf))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaTicketRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaTicketRegistryCoreConfiguration {
        @ConditionalOnMissingBean(name = "jpaTicketRegistryTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations jpaTicketRegistryTransactionTemplate(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("ticketTransactionManager")
            final PlatformTransactionManager ticketTransactionManager,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TransactionOperations.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val template = new TransactionTemplate(ticketTransactionManager);
                    val jpa = casProperties.getTicket().getRegistry().getJpa();
                    template.setIsolationLevelName(jpa.getIsolationLevelName());
                    template.setPropagationBehaviorName(jpa.getPropagationBehaviorName());
                    return template;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketRegistry ticketRegistry(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaTicketRegistryTransactionTemplate")
            final TransactionTemplate jpaTicketRegistryTransactionTemplate,
            @Qualifier(TicketCatalog.BEAN_NAME)
            final TicketCatalog ticketCatalog,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(TicketRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val jpa = casProperties.getTicket().getRegistry().getJpa();
                    val bean = new JpaTicketRegistry(jpa.getTicketLockType(), ticketCatalog,
                        jpaBeanFactory, jpaTicketRegistryTransactionTemplate, casProperties);
                    bean.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa"));
                    return bean;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JpaTicketRegistryLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.TicketRegistryLocking, module = "jpa")
    public static class JpaTicketRegistryLockingConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.ticket.registry.core.enable-locking").isTrue().evenIfMissing();

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public org.springframework.integration.jdbc.lock.LockRepository jdbcLockRepository(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket) {
            return BeanSupplier.of(org.springframework.integration.jdbc.lock.LockRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new org.springframework.integration.jdbc.lock.DefaultLockRepository(dataSourceTicket))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry jdbcLockRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcLockRepository")
            final org.springframework.integration.jdbc.lock.LockRepository jdbcLockRepository) {
            return BeanSupplier.of(LockRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new JdbcLockRegistry(jdbcLockRepository))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcLockRegistry")
            final LockRegistry jdbcLockRegistry) {
            return BeanSupplier.of(LockRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultLockRepository(jdbcLockRegistry))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        InitializingBean casTicketRegistryLockDataSourceScriptDatabaseInitializer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dataSourceTicket")
            final CloseableDataSource dataSourceTicket,
            final IntegrationProperties properties) {
            return BeanSupplier.of(InitializingBean.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new IntegrationDataSourceScriptDatabaseInitializer(dataSourceTicket, properties.getJdbc()))
                .otherwiseProxy()
                .get();
        }
    }
}
