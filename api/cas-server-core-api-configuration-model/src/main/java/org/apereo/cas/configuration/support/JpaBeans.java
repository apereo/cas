package org.apereo.cas.configuration.support;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.config.DefaultJpaEntityManagerFactoryBeanFactory;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.support.jpa.JpaEntityManagerFactoryBeanFactory;
import org.apereo.cas.support.jpa.JpaRuntimeException;
import org.hibernate.cfg.Environment;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * This is {@link JpaBeans}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */

@Slf4j
@UtilityClass
public class JpaBeans {

    /**
     * Get new data source, for JDBC.
     *
     * @param jpa the jpa properties
     * @return the data source
     */
    public static DataSource newDataSource(final AbstractJpaProperties jpa) {
        switch (jpa.getType()){
            case JDBC:
            default:
                return newJdbcDataSource(jpa);
        }
    }

    /**
     * Get new data source, from JNDI lookup or created via direct configuration
     * of Hikari pool.
     * <p>
     * If properties specify a data source name, a lookup will be
     * attempted. If the DataSource is not found via JNDI then CAS will attempt to
     * configure a Hikari connection pool.
     * <p>
     * Since the datasource beans are {@link org.springframework.cloud.context.config.annotation.RefreshScope},
     * they will be a proxied by Spring
     * and on some application servers there have been classloading issues. A workaround
     * for this is to use activate data source proxying via settings and then the dataSource will be
     * wrapped in an application level class. If that is an issue, don't do it.
     * <p>
     * If user wants to do lookup as resource, they may include {@code java:/comp/env}
     * in {@code dataSourceName} and put resource reference in web.xml
     * otherwise {@code dataSourceName} is used as JNDI name.
     *
     * @param jpaProperties the jpa properties
     * @return the data source
     */
    @SneakyThrows
    private static DataSource newJdbcDataSource(final AbstractJpaProperties jpaProperties) {
        final String dataSourceName = jpaProperties.getDataSourceName();
        final boolean proxyDataSource = jpaProperties.isDataSourceProxy();

        if (StringUtils.isNotBlank(dataSourceName)) {
            try {
                final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
                dsLookup.setResourceRef(false);
                final DataSource containerDataSource = dsLookup.getDataSource(dataSourceName);
                if (!proxyDataSource) {
                    return containerDataSource;
                }
                return new DataSourceProxy(containerDataSource);
            } catch (final DataSourceLookupFailureException e) {
                LOGGER.warn("Lookup of datasource [{}] failed due to {} falling back to configuration via JPA properties.", dataSourceName, e.getMessage());
            }
        }

        final HikariDataSource bean = new HikariDataSource();
        if (StringUtils.isNotBlank(jpaProperties.getDriverClass())) {
            bean.setDriverClassName(jpaProperties.getDriverClass());
        }
        bean.setJdbcUrl(jpaProperties.getUrl());
        bean.setUsername(jpaProperties.getUser());
        bean.setPassword(jpaProperties.getPassword());
        bean.setLoginTimeout((int) Beans.newDuration(jpaProperties.getPool().getMaxWait()).getSeconds());
        bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
        bean.setMinimumIdle(jpaProperties.getPool().getMinSize());
        bean.setIdleTimeout((int) Beans.newDuration(jpaProperties.getIdleTimeout()).toMillis());
        bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
        bean.setInitializationFailTimeout(jpaProperties.getFailFastTimeout());
        bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
        bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
        bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
        bean.setAutoCommit(jpaProperties.isAutocommit());
        bean.setValidationTimeout(jpaProperties.getPool().getTimeoutMillis());
        return bean;
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
     * New entity manager factory bean. @deprecated Being replaced with one to decouple EntityManagerFactoryBean creation.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    @Deprecated
    public static LocalContainerEntityManagerFactoryBean newHibernateEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                              final AbstractJpaProperties jpaProperties) {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan().toArray(new String[]{}));

        if (config.getDataSource() != null) {
            bean.setDataSource(config.getDataSource());
        }

        final Properties properties = new Properties();
        properties.put(Environment.DIALECT, jpaProperties.getDialect());
        properties.put(Environment.HBM2DDL_AUTO, jpaProperties.getDdlAuto());
        properties.put(Environment.STATEMENT_BATCH_SIZE, jpaProperties.getBatchSize());
        putPropertyUnlessBlank(properties, Environment.DEFAULT_SCHEMA, jpaProperties.getDefaultSchema());
        putPropertyUnlessBlank(properties, Environment.DEFAULT_CATALOG, jpaProperties.getDefaultCatalog());
        properties.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, Boolean.TRUE);
        properties.put(Environment.FORMAT_SQL, Boolean.TRUE);
        properties.putAll(jpaProperties.getProperties());
        bean.setJpaProperties(properties);

        return bean;
    }

    /**
     * Creates new entitfy manager factory.
     * @param factoryBeanFactory decoupled map for jpa type
     * @param configDataHolder configuration details holder
     * @param jpaProperties jpa properties
     * @return EntityManagerFactoryBean fully configured
     */
    public static LocalContainerEntityManagerFactoryBean newHibernateEntityManagerFactoryBean(
        final DefaultJpaEntityManagerFactoryBeanFactory factoryBeanFactory,
        final JpaConfigDataHolder configDataHolder, final AbstractJpaProperties jpaProperties) {

        final JpaEntityManagerFactoryBeanFactory jpaEmfbf = factoryBeanFactory.getFactoryForType(jpaProperties.getType());

        if (jpaEmfbf == null) {
            throw new JpaRuntimeException(String.format("No EntityManagerFactoryBean for [%s]", String.valueOf(jpaProperties.getType())));
        }

        return jpaEmfbf.newEntityManagerFactoryBean(configDataHolder, jpaProperties);
    }

    /**
     * Add propery, with optional default, if set.
     * @param properties Properties to add to
     * @param name property name to add
     * @param value property value to add
     */
    public static void putPropertyUnlessBlank(final Properties properties, final String name, final String value) {
        putPropertyUnlessBlank(properties, name, value, null);
    }

    /**
     * Add propery, with optional default, if set.
     * @param properties Properties to add to
     * @param name property name to add
     * @param value property value to add
     */
    public static void putPropertyUnlessBlank(final Properties properties, final String name, final Integer value) {
        putPropertyUnlessBlank(properties, name, value, null);
    }

    /**
     * Add propery, with optional default, if set.
     * @param properties Properties to add to
     * @param name property name to add
     * @param value property value to add
     * @param defaultValue default value, ignored if +null+
     */
    public static void putPropertyUnlessBlank(final Properties properties, final String name, final String value, final String defaultValue) {
        if (StringUtils.isNotBlank(value)) {
            LOGGER.debug("Property [{}] set to [{}].", name, value);
            properties.put(name, value);
        } else if (StringUtils.isNotBlank(defaultValue)) {
            LOGGER.warn("Property [{}] not set: failover to [{}].", name, defaultValue);
            properties.put(name, defaultValue);
        } else {
            LOGGER.debug("Property [{}] not set: using default [{}].", name, properties.get(name));
        }
    }

    /**
     * Add propery, with optional default, if set.
     * @param properties Properties to add to
     * @param name property name to add
     * @param value property value to add
     * @param defaultValue default value, ignored if +null+
     */
    public static void putPropertyUnlessBlank(final Properties properties, final String name, final Integer value, final Integer defaultValue) {
        if (value != null) {
            LOGGER.debug("Property [{}] set to [{}].", name, value);
            properties.put(name, value);
        } else if (defaultValue != null) {
            LOGGER.warn("Property [{}] not set: failover to [{}].", name, defaultValue);
            properties.put(name, defaultValue);
        } else {
            LOGGER.trace("Property [{}] not set: no failover.", name);
        }
    }
}
