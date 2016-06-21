package org.apereo.cas.configuration.support;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * <code>@Bean</code> creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class Beans {

    protected Beans() {
    }

    /**
     * New hickari data source.
     *
     * @param jpaProperties the jpa properties
     * @return the hikari data source
     */
    public static HikariDataSource newHickariDataSource(final AbstractJpaProperties jpaProperties) {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(jpaProperties.getDriverClass());
            bean.setJdbcUrl(jpaProperties.getUrl());
            bean.setUsername(jpaProperties.getUser());
            bean.setPassword(jpaProperties.getPassword());

            bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
            bean.setMinimumIdle(jpaProperties.getPool().getMaxIdleTime());
            bean.setIdleTimeout(jpaProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
            bean.setInitializationFailFast(jpaProperties.isFailFast());
            bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
            bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
            bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
            bean.setAutoCommit(jpaProperties.isAutocommit());
            bean.setLoginTimeout(jpaProperties.getPool().getMaxWait());
            bean.setValidationTimeout(jpaProperties.getPool().getMaxWait());
            return bean;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * New hibernate jpa vendor adapter.
     *
     * @param databaseProperties the database properties
     * @return the hibernate jpa vendor adapter
     */
    public static HibernateJpaVendorAdapter newHibernateJpaVendorAdapter(final DatabaseProperties databaseProperties) {
        final HibernateJpaVendorAdapter bean = new HibernateJpaVendorAdapter();
        bean.setGenerateDdl(databaseProperties.isGenDdl());
        bean.setShowSql(databaseProperties.isShowSql());
        return bean;
    }

    /**
     * New thread pool executor factory bean thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(config.getMinSize());
        bean.setMaxPoolSize(config.getMaxSize());
        bean.setKeepAliveSeconds(config.getMaxWait());
        return bean;
    }
    
    /**
     * New entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                     final AbstractJpaProperties jpaProperties) {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotEmpty(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan());
        bean.setDataSource(config.getDataSource());

        final Properties properties = new Properties();
        properties.put("hibernate.dialect", jpaProperties.getDialect());
        properties.put("hibernate.hbm2ddl.auto", jpaProperties.getDdlAuto());
        properties.put("hibernate.jdbc.batch_size", jpaProperties.getBatchSize());
        bean.setJpaProperties(properties);
        return bean;
    }

    /**
     * New attribute repository person attribute dao.
     *
     * @param attributes the attributes
     * @return the person attribute dao
     */
    public static IPersonAttributeDao newAttributeRepository(final Map<String, String> attributes) {
        try {
            final NamedStubPersonAttributeDao dao = new NamedStubPersonAttributeDao();
            final Map pdirMap = new HashMap<>();
            attributes.entrySet().forEach(entry -> {
                pdirMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
            });
            dao.setBackingMap(pdirMap);
            return dao;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
