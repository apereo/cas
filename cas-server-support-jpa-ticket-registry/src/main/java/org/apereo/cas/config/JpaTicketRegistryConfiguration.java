package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategy;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.util.InetAddressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.apereo.cas.configuration.support.Beans.newEntityManagerFactoryBean;
import static org.apereo.cas.configuration.support.Beans.newHibernateJpaVendorAdapter;
import static org.apereo.cas.configuration.support.Beans.newHickariDataSource;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
public class JpaTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor cipherExecutor;
    
    /**
     * Jpa packages to scan string [].
     *
     * @return the string [ ]
     */
    @Bean
    public String[] ticketPackagesToScan() {
        return new String[] {
                "org.apereo.cas.ticket",
                "org.apereo.cas.adaptors.jdbc"
        };
    }

    /**
     * Entity manager factory local container.
     *
     * @return the local container entity manager factory bean
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory() {
        return newEntityManagerFactoryBean(
                new JpaConfigDataHolder(
                        newHibernateJpaVendorAdapter(casProperties.getJdbc()),
                        "jpaTicketRegistryContext",
                        ticketPackagesToScan(),
                        dataSourceTicket()),
                        casProperties.getTicket().getRegistry().getJpa());
    }

    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     *
     * @return the jpa transaction manager
     */
    @Bean
    public PlatformTransactionManager ticketTransactionManager(@Qualifier("ticketEntityManagerFactory")
                                                          final EntityManagerFactory emf) {

        
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    /**
     * Data source ticket combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @RefreshScope
    @Bean
    public DataSource dataSourceTicket() {
        return newHickariDataSource(casProperties.getTicket().getRegistry().getJpa());
    }

    
    @Bean(name = {"jpaTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry jpaTicketRegistry() {
        final JpaTicketRegistry bean = new JpaTicketRegistry();
        bean.setLockTgt(casProperties.getTicket().getRegistry().getJpa().isJpaLockingTgtEnabled());
        bean.setCipherExecutor(this.cipherExecutor);
        return bean;
    }

    @Bean
    public LockingStrategy lockingStrategy() {
        final JpaLockingStrategy bean = new JpaLockingStrategy();
        bean.setApplicationId(casProperties.getTicket().getRegistry().getCleaner().getAppId());
        bean.setUniqueId(StringUtils.defaultIfEmpty(casProperties.getHost().getName(), 
                InetAddressUtils.getCasServerHostName()));
        bean.setLockTimeout(casProperties.getTicket().getRegistry().getJpa().getJpaLockingTimeout());
        return bean;
    }
}
