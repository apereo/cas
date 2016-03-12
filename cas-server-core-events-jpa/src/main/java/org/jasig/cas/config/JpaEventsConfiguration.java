package org.jasig.cas.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * This is {@link JpaEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("jpaEventsConfiguration")
@Lazy(true)
public class JpaEventsConfiguration {

    /**
     * The Show sql.
     */
    @Value("${database.show.sql:true}")
    private boolean showSql;

    /**
     * The Generate ddl.
     */
    @Value("${database.gen.ddl:true}")
    private boolean generateDdl;

    /**
     * The Hibernate dialect.
     */
    @Value("${events.jpa.database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;

    /**
     * The Hibernate hbm 2 ddl auto.
     */
    @Value("${events.jpa.database.ddl.auto:create-drop}")
    private String hibernateHbm2DdlAuto;

    /**
     * The Hibernate batch size.
     */
    @Value("${events.jpa.database.batchSize:1}")
    private String hibernateBatchSize;

    /**
     * The Driver class.
     */
    @Value("${events.jpa.database.driverClass:org.hsqldb.jdbcDriver}")
    private String driverClass;

    /**
     * The Jdbc url.
     */
    @Value("${events.jpa.database.url:jdbc:hsqldb:mem:cas-events-registry}")
    private String jdbcUrl;

    /**
     * The User.
     */
    @Value("${events.jpa.database.user:sa}")
    private String user;

    /**
     * The Password.
     */
    @Value("${events.jpa.database.password:}")
    private String password;

    /**
     * The Initial pool size.
     */
    @Value("${events.jpa.database.pool.minSize:6}")
    private int initialPoolSize;

    /**
     * The Min pool size.
     */
    @Value("${events.jpa.database.pool.minSize:6}")
    private int minPoolSize;

    /**
     * The Max pool size.
     */
    @Value("${events.jpa.database.pool.maxSize:18}")
    private int maxPoolSize;

    /**
     * The Max idle time excess connections.
     */
    @Value("${events.jpa.database.pool.maxIdleTime:1000}")
    private int maxIdleTimeExcessConnections;

    /**
     * The Checkout timeout.
     */
    @Value("${events.jpa.database.pool.maxWait:2000}")
    private int checkoutTimeout;

    /**
     * The Acquire increment.
     */
    @Value("${events.jpa.database.pool.acquireIncrement:16}")
    private int acquireIncrement;

    /**
     * The Acquire retry attempts.
     */
    @Value("${events.jpa.database.pool.acquireRetryAttempts:5}")
    private int acquireRetryAttempts;

    /**
     * The Acquire retry delay.
     */
    @Value("${events.jpa.database.pool.acquireRetryDelay:2000}")
    private int acquireRetryDelay;

    /**
     * The Idle connection test period.
     */
    @Value("${events.jpa.database.pool.idleConnectionTestPeriod:30}")
    private int idleConnectionTestPeriod;

    /**
     * The Preferred test query.
     */
    @Value("${events.jpa.database.pool.connectionHealthQuery:select 1}")
    private String preferredTestQuery;


    /**
     * Jpa event vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @Bean(name = "jpaEventVendorAdapter")
    public HibernateJpaVendorAdapter jpaEventVendorAdapter() {
        final HibernateJpaVendorAdapter jpaEventVendorAdapter = new HibernateJpaVendorAdapter();
        jpaEventVendorAdapter.setGenerateDdl(this.generateDdl);
        jpaEventVendorAdapter.setShowSql(this.showSql);
        return jpaEventVendorAdapter;
    }


    /**
     * Data source event combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @Bean(name = "dataSourceEvent")
    public ComboPooledDataSource dataSourceEvent() {
        try {
            final ComboPooledDataSource bean = new ComboPooledDataSource();
            bean.setDriverClass(this.driverClass);
            bean.setJdbcUrl(this.jdbcUrl);
            bean.setUser(this.user);
            bean.setPassword(this.password);
            bean.setInitialPoolSize(this.initialPoolSize);
            bean.setMinPoolSize(this.minPoolSize);
            bean.setMaxPoolSize(this.maxPoolSize);
            bean.setMaxIdleTimeExcessConnections(this.maxIdleTimeExcessConnections);
            bean.setCheckoutTimeout(this.checkoutTimeout);
            bean.setAcquireIncrement(this.acquireIncrement);
            bean.setAcquireRetryAttempts(this.acquireRetryAttempts);
            bean.setAcquireRetryDelay(this.acquireRetryDelay);
            bean.setIdleConnectionTestPeriod(this.idleConnectionTestPeriod);
            bean.setPreferredTestQuery(this.preferredTestQuery);
            return bean;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Jpa event packages to scan string [ ].
     *
     * @return the string [ ]
     */
    @Bean(name = "jpaEventPackagesToScan")
    public String[] jpaEventPackagesToScan() {
        return new String[]{"org.jasig.cas.support.events.dao"};
    }

    /**
     * Events entity manager factory local container entity manager factory bean.
     *
     * @return the local container entity manager factory bean
     */
    @Bean(name = "eventsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(jpaEventVendorAdapter());
        bean.setPersistenceUnitName("jpaEventRegistryContext");
        bean.setPackagesToScan(jpaEventPackagesToScan());
        bean.setDataSource(dataSourceEvent());

        final Properties properties = new Properties();
        properties.put("hibernate.dialect", this.hibernateDialect);
        properties.put("hibernate.hbm2ddl.auto", this.hibernateHbm2DdlAuto);
        properties.put("hibernate.jdbc.batch_size", this.hibernateBatchSize);
        properties.put("hibernate.enable_lazy_load_no_trans", Boolean.TRUE);
        bean.setJpaProperties(properties);
        return bean;
    }


    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     * @return the jpa transaction manager
     */
    @Bean(name = "transactionManagerEvents")
    public JpaTransactionManager transactionManagerEvents(@Qualifier("eventsEntityManagerFactory")
                                                          final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


}
