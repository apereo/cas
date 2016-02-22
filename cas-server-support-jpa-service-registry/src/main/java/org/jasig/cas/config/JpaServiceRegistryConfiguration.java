package org.jasig.cas.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * This this {@link JpaServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("jpaServiceRegistryConfiguration")
public class JpaServiceRegistryConfiguration {

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
    @Value("${svcreg.database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;

    /**
     * The Hibernate hbm 2 ddl auto.
     */
    @Value("${svcreg.database.ddl.auto:create-drop}")
    private String hibernateHbm2DdlAuto;

    /**
     * The Hibernate batch size.
     */
    @Value("${svcreg.database.batchSize:1}")
    private String hibernateBatchSize;


    /**
     * The Driver class.
     */
    @Value("${svcreg.database.driverClass:org.hsqldb.jdbcDriver}")
    private String driverClass;

    /**
     * The Jdbc url.
     */
    @Value("${svcreg.database.url:jdbc:hsqldb:mem:cas-service-registry}")
    private String jdbcUrl;

    /**
     * The User.
     */
    @Value("${svcreg.database.user:sa}")
    private String user;

    /**
     * The Password.
     */
    @Value("${svcreg.database.password:}")
    private String password;

    /**
     * The Initial pool size.
     */
    @Value("${svcreg.database.pool.minSize:6}")
    private int initialPoolSize;

    /**
     * The Min pool size.
     */
    @Value("${svcreg.database.pool.minSize:6}")
    private int minPoolSize;

    /**
     * The Max pool size.
     */
    @Value("${svcreg.database.pool.maxSize:18}")
    private int maxPoolSize;

    /**
     * The Max idle time excess connections.
     */
    @Value("${svcreg.database.pool.maxIdleTime:1000}")
    private int maxIdleTimeExcessConnections;

    /**
     * The Checkout timeout.
     */
    @Value("${svcreg.database.pool.maxWait:2000}")
    private int checkoutTimeout;

    /**
     * The Acquire increment.
     */
    @Value("${svcreg.database.pool.acquireIncrement:16}")
    private int acquireIncrement;

    /**
     * The Acquire retry attempts.
     */
    @Value("${svcreg.database.pool.acquireRetryAttempts:5}")
    private int acquireRetryAttempts;

    /**
     * The Acquire retry delay.
     */
    @Value("${svcreg.database.pool.acquireRetryDelay:2000}")
    private int acquireRetryDelay;

    /**
     * The Idle connection test period.
     */
    @Value("${svcreg.database.pool.idleConnectionTestPeriod:30}")
    private int idleConnectionTestPeriod;

    /**
     * The Preferred test query.
     */
    @Value("${svcreg.database.pool.connectionHealthQuery:select 1}")
    private String preferredTestQuery;

    /**
     * Jpa vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @Bean(name = "jpaServiceVendorAdapter")
    public HibernateJpaVendorAdapter jpaServiceVendorAdapter() {
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
    @Bean(name = "jpaServicePackagesToScan")
    public String[] jpaServicePackagesToScan() {
        return new String[] {
                "org.jasig.cas.services", 
                "org.jasig.cas.support.oauth.services",
                "org.jasig.cas.support.saml.services"
        };
    }

    /**
     * Entity manager factory local container.
     *
     * @return the local container entity manager factory bean
     */
    @Bean(name = "serviceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean serviceEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(jpaServiceVendorAdapter());
        bean.setPersistenceUnitName("jpaServiceRegistryContext");
        bean.setPackagesToScan(jpaServicePackagesToScan());
        bean.setDataSource(dataSourceService());

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
    @Bean(name = "transactionManagerServiceReg")
    public JpaTransactionManager transactionManagerServiceReg(@Qualifier("serviceEntityManagerFactory") 
                                                          final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    /**
     * Data source service combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @Bean(name = "dataSourceService")
    public ComboPooledDataSource dataSourceService() {
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
}
