package org.apereo.cas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaTicketRegistryConfiguration")
public class JpaTicketRegistryConfiguration {
    
    @Value("${database.show.sql:true}")
    private boolean showSql;
    
    @Value("${database.gen.ddl:true}")
    private boolean generateDdl;
    
    @Value("${ticketreg.database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;
    
    @Value("${ticketreg.database.ddl.auto:create-drop}")
    private String hibernateHbm2DdlAuto;
    
    @Value("${ticketreg.database.batchSize:1}")
    private String hibernateBatchSize;
    
    @Value("${ticketreg.database.driverClass:org.hsqldb.jdbcDriver}")
    private String driverClass;
    
    @Value("${ticketreg.database.url:jdbc:hsqldb:mem:cas-service-registry}")
    private String jdbcUrl;
    
    @Value("${ticketreg.database.user:sa}")
    private String user;
    
    @Value("${ticketreg.database.password:}")
    private String password;
    
    @Value("${ticketreg.database.pool.minSize:6}")
    private int initialPoolSize;
    
    @Value("${ticketreg.database.pool.maxSize:18}")
    private int maxPoolSize;
    
    @Value("${ticketreg.database.pool.maxIdleTime:1000}")
    private int maxIdleTimeExcessConnections;

    @Value("${ticketreg.database.pool.maxWait:2000}")
    private int checkoutTimeout;

    @Value("${ticketreg.database.idle.timeout:5000}")
    private int idleTimeout;

    @Value("${ticketreg.database.leak.threshold:10}")
    private int leakDetectionThreshold;

    @Value("${ticketreg.database.fail.fast:true}")
    private boolean failFast;

    @Value("${ticketreg.database.isolate.internal.queries:false}")
    private boolean isolateInternalQueries;

    @Value("${ticketreg.database.health.query:SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS}")
    private String healthCheckQuery;

    @Value("${ticketreg.database.pool.suspension:false}")
    private boolean allowPoolSuspension;

    @Value("${ticketreg.database.autocommit:false}")
    private boolean autoCommit;
        
    /**
     * Jpa vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @Bean(name = "ticketJpaVendorAdapter")
    public HibernateJpaVendorAdapter ticketJpaVendorAdapter() {
        final HibernateJpaVendorAdapter jpaEventVendorAdapter = new HibernateJpaVendorAdapter();
        jpaEventVendorAdapter.setGenerateDdl(this.generateDdl);
        jpaEventVendorAdapter.setShowSql(this.showSql);
        return jpaEventVendorAdapter;
    }

    /**
     * Jpa packages to scan string [].
     *
     * @return the string [ ]
     */
    @Bean(name = "ticketPackagesToScan")
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
    @Bean(name = "ticketEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(ticketJpaVendorAdapter());
        bean.setPersistenceUnitName("jpaTicketRegistryContext");
        bean.setPackagesToScan(ticketPackagesToScan());
        bean.setDataSource(dataSourceTicket());
        final Properties properties = new Properties();
        properties.put("hibernate.dialect", this.hibernateDialect);
        properties.put("hibernate.hbm2ddl.auto", this.hibernateHbm2DdlAuto);
        properties.put("hibernate.jdbc.batch_size", this.hibernateBatchSize);
        bean.setJpaProperties(properties);
        return bean;
    }

    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     * @return the jpa transaction manager
     */
    @Bean(name = "ticketTransactionManager")
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
    @Bean(name = "dataSourceTicket")
    public DataSource dataSourceTicket() {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(this.driverClass);
            bean.setJdbcUrl(this.jdbcUrl);
            bean.setUsername(this.user);
            bean.setPassword(this.password);

            bean.setMaximumPoolSize(this.maxPoolSize);
            bean.setMinimumIdle(this.maxIdleTimeExcessConnections);
            bean.setIdleTimeout(this.idleTimeout);
            bean.setLeakDetectionThreshold(this.leakDetectionThreshold);
            bean.setInitializationFailFast(this.failFast);
            bean.setIsolateInternalQueries(this.isolateInternalQueries);
            bean.setConnectionTestQuery(this.healthCheckQuery);
            bean.setAllowPoolSuspension(this.allowPoolSuspension);
            bean.setAutoCommit(this.autoCommit);
            bean.setLoginTimeout(this.checkoutTimeout);
            bean.setValidationTimeout(this.checkoutTimeout);
            return bean;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
