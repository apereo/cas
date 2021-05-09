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

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import java.util.Set;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureBefore(CasCoreTicketsConfiguration.class)
public class JpaTicketRegistryConfiguration {

    @Autowired
    @Qualifier("ticketCatalog")
    private ObjectProvider<TicketCatalog> ticketCatalog;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Set<String> ticketPackagesToScan() {
        val jpa = casProperties.getTicket().getRegistry().getJpa();
        val type = new JpaTicketEntityFactory(jpa.getDialect()).getType();
        return CollectionUtils.wrapSet(
            type.getPackage().getName(),
            JpaLockEntity.class.getPackage().getName());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val factory = jpaBeanFactory.getObject();
        val ctx = JpaConfigurationContext.builder()
            .jpaVendorAdapter(jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc()))
            .persistenceUnitName("jpaTicketRegistryContext")
            .dataSource(dataSourceTicket())
            .packagesToScan(ticketPackagesToScan())
            .build();
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getTicket().getRegistry().getJpa());
    }

    @ConditionalOnMissingBean(name = JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER)
    @Bean
    public PlatformTransactionManager ticketTransactionManager(@Qualifier("ticketEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceTicket")
    @RefreshScope
    public CloseableDataSource dataSourceTicket() {
        return JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa());
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val jpa = casProperties.getTicket().getRegistry().getJpa();
        val bean = new JpaTicketRegistry(jpa.getTicketLockType(), ticketCatalog.getObject(),
            jpaBeanFactory.getObject(), jpaTicketRegistryTransactionTemplate(), casProperties);
        bean.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa"));
        return bean;
    }

    @Bean
    public LockingStrategy lockingStrategy() {
        val registry = casProperties.getTicket().getRegistry();
        val uniqueId = StringUtils.defaultIfEmpty(casProperties.getHost().getName(), InetAddressUtils.getCasServerHostName());
        return new JpaLockingStrategy("cas-ticket-registry-cleaner", uniqueId,
            Beans.newDuration(registry.getJpa().getJpaLockingTimeout()).getSeconds());
    }

    @ConditionalOnMissingBean(name = "jpaTicketRegistryTransactionTemplate")
    @Bean
    public TransactionTemplate jpaTicketRegistryTransactionTemplate() {
        var mgr = applicationContext.getBean(JpaTicketRegistry.BEAN_NAME_TRANSACTION_MANAGER, PlatformTransactionManager.class);
        val t = new TransactionTemplate(mgr);
        val jpa = casProperties.getTicket().getRegistry().getJpa();
        t.setIsolationLevelName(jpa.getIsolationLevelName());
        t.setPropagationBehaviorName(jpa.getPropagationBehaviorName());
        return t;
    }
}
