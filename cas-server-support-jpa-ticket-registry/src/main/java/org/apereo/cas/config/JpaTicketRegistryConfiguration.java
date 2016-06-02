package org.apereo.cas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategy;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaTicketRegistryConfiguration")
@EnableConfigurationProperties({JpaTicketRegistryProperties.class, DatabaseProperties.class})
public class JpaTicketRegistryConfiguration {
    
    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private JpaTicketRegistryProperties jpaTicketRegistryProperties;
        
    /**
     * Jpa vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    
    public HibernateJpaVendorAdapter ticketJpaVendorAdapter() {
        final HibernateJpaVendorAdapter jpaEventVendorAdapter = new HibernateJpaVendorAdapter();
        jpaEventVendorAdapter.setGenerateDdl(this.databaseProperties.isGenDdl());
        jpaEventVendorAdapter.setShowSql(this.databaseProperties.isShowSql());
        return jpaEventVendorAdapter;
    }

    /**
     * Jpa packages to scan string [].
     *
     * @return the string [ ]
     */
    
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
    
    public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(ticketJpaVendorAdapter());
        bean.setPersistenceUnitName("jpaTicketRegistryContext");
        bean.setPackagesToScan(ticketPackagesToScan());
        bean.setDataSource(dataSourceTicket());
        final Properties properties = new Properties();
        properties.put("hibernate.dialect", this.jpaTicketRegistryProperties.getDialect());
        properties.put("hibernate.hbm2ddl.auto", this.jpaTicketRegistryProperties.getDdlAuto());
        properties.put("hibernate.jdbc.batch_size", this.jpaTicketRegistryProperties.getBatchSize());
        bean.setJpaProperties(properties);
        return bean;
    }

    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     * @return the jpa transaction manager
     */
    
    public JpaTransactionManager ticketTransactionManager(@Qualifier("ticketEntityManagerFactory") 
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
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(this.jpaTicketRegistryProperties.getDriverClass());
            bean.setJdbcUrl(this.jpaTicketRegistryProperties.getUrl());
            bean.setUsername(this.jpaTicketRegistryProperties.getUser());
            bean.setPassword(this.jpaTicketRegistryProperties.getPassword());

            bean.setMaximumPoolSize(this.jpaTicketRegistryProperties.getPool().getMaxSize());
            bean.setMinimumIdle(this.jpaTicketRegistryProperties.getPool().getMaxIdleTime());
            bean.setIdleTimeout(this.jpaTicketRegistryProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(this.jpaTicketRegistryProperties.getLeakThreshold());
            bean.setInitializationFailFast(this.jpaTicketRegistryProperties.isFailFast());
            bean.setIsolateInternalQueries(this.jpaTicketRegistryProperties.isolateInternalQueries());
            bean.setConnectionTestQuery(this.jpaTicketRegistryProperties.getHealthQuery());
            bean.setAllowPoolSuspension(this.jpaTicketRegistryProperties.getPool().isSuspension());
            bean.setAutoCommit(this.jpaTicketRegistryProperties.isAutocommit());
            bean.setLoginTimeout(this.jpaTicketRegistryProperties.getPool().getMaxWait());
            bean.setValidationTimeout(this.jpaTicketRegistryProperties.getPool().getMaxWait());
            return bean;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Bean
    @RefreshScope
    public TicketRegistry jpaTicketRegistry() {
        return new JpaTicketRegistry();
    }
    
    @Bean
    public LockingStrategy jpaLockingStrategy() {
        return new JpaLockingStrategy();
    }
}
