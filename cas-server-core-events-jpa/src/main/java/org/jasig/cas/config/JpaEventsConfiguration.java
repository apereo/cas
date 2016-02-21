package org.jasig.cas.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class JpaEventsConfiguration {

    @Value("${database.show.sql:true}")
    private boolean showSql;

    @Value("${database.gen.ddl:true}")
    private boolean generateDdl;

    @Value("${events.jpa.database.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;

    @Value("${events.jpa.database.ddl.auto:create-drop}")
    private String hibernateHbm2DdlAuto;

    @Value("${events.jpa.database.batchSize:1}")
    private String hibernateBatchSize;

    @Value("${events.jpa.database.driverClass:org.hsqldb.jdbcDriver}")
    private String driverClass;

    @Value("${events.jpa.database.url:jdbc:hsqldb:mem:cas-events-registry}")
    private String jdbcUrl;

    @Value("${events.jpa.database.user:sa}")
    private String user;

    @Value("${events.jpa.database.password:}")
    private String password;

    @Value("${events.jpa.database.pool.minSize:6}")
    private int initialPoolSize;

    @Value("${events.jpa.database.pool.minSize:6}")
    private int minPoolSize;

    @Value("${events.jpa.database.pool.maxSize:18}")
    private int maxPoolSize;

    @Value("${events.jpa.database.pool.maxIdleTime:1000}")
    private int maxIdleTimeExcessConnections;

    @Value("${events.jpa.database.pool.maxWait:2000}")
    private int checkoutTimeout;

    @Value("${events.jpa.database.pool.acquireIncrement:16}")
    private int acquireIncrement;

    @Value("${events.jpa.database.pool.acquireRetryAttempts:5}")
    private int acquireRetryAttempts;

    @Value("${events.jpa.database.pool.acquireRetryDelay:2000}")
    private int acquireRetryDelay;

    @Value("${events.jpa.database.pool.idleConnectionTestPeriod:30}")
    private int idleConnectionTestPeriod;

    @Value("${events.jpa.database.pool.connectionHealthQuery:select 1}")
    private String preferredTestQuery;


    @Bean(name = "jpaEventVendorAdapter")
    public HibernateJpaVendorAdapter jpaEventVendorAdapter() {
        final HibernateJpaVendorAdapter jpaEventVendorAdapter = new HibernateJpaVendorAdapter();
        jpaEventVendorAdapter.setGenerateDdl(this.generateDdl);
        jpaEventVendorAdapter.setShowSql(this.showSql);
        return jpaEventVendorAdapter;
    }


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

    @Bean(name = "jpaEventPackagesToScan")
    public String[] jpaEventPackagesToScan() {
        return new String[]{"org.jasig.cas.support.events.dao"};
    }

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


    @Bean(name = "transactionManagerEvents")
    public JpaTransactionManager transactionManagerEvents(final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


}
