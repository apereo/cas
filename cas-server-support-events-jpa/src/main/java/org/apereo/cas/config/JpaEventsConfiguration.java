package org.apereo.cas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apereo.cas.configuration.model.core.EventsProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.support.events.jpa.JpaCasEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * This is {@link JpaEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaEventsConfiguration")
@EnableConfigurationProperties({EventsProperties.class, DatabaseProperties.class})
public class JpaEventsConfiguration {

    @Autowired
    DatabaseProperties databaseProperties;

    @Autowired
    EventsProperties eventsProperties;


    /**
     * Jpa event vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */
    @RefreshScope
    @Bean
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
    @RefreshScope
    @Bean
    public DataSource dataSourceEvent() {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(this.driverClass);
            bean.setJdbcUrl(this.jdbcUrl);
            bean.setUsername(this.user);
            bean.setPassword(this.password);

            bean.setMaximumPoolSize(this.maxPoolSize);
            bean.setMinimumIdle(this.maxIdleTimeExcessConnections);

            bean.setLoginTimeout(this.checkoutTimeout);
            bean.setValidationTimeout(this.checkoutTimeout);

            return bean;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Jpa event packages to scan string [ ].
     *
     * @return the string [ ]
     */

    public String[] jpaEventPackagesToScan() {
        return new String[] {"org.apereo.cas.support.events.dao"};
    }

    /**
     * Events entity manager factory local container entity manager factory bean.
     *
     * @return the local container entity manager factory bean
     */
    @RefreshScope
    @Bean
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
        //TODO
        bean.getJpaPropertyMap();
        return bean;
    }


    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     *
     * @return the jpa transaction manager
     */
    @Autowired
    @Bean
    public JpaTransactionManager transactionManagerEvents(@Qualifier("eventsEntityManagerFactory")
                                                          final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


    @Bean
    public CasEventRepository casEventRepository() {
        return new JpaCasEventRepository();
    }
}
