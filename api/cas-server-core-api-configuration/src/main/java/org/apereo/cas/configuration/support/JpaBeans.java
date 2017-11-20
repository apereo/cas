package org.apereo.cas.configuration.support;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class JpaBeans {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaBeans.class);
    
    protected JpaBeans() {
    }

    /**
     * Get new data source, from JNDI lookup or created via direct configuration
     * of Hikari pool.
     * <p>
     * If jpaProperties contains {@link AbstractJpaProperties#getDataSourceName()} a lookup will be
     * attempted. If the DataSource is not found via JNDI then CAS will attempt to
     * configure a Hikari connection pool.
     * <p>
     * Since the datasource beans are {@link org.springframework.cloud.context.config.annotation.RefreshScope},
     * they will be a proxied by Spring
     * and on some application servers there have been classloading issues. A workaround
     * for this is to use the {@link AbstractJpaProperties#isDataSourceProxy()} setting and then the dataSource will be
     * wrapped in an application level class. If that is an issue, don't do it.
     * <p>
     * If user wants to do lookup as resource, they may include {@code java:/comp/env}
     * in {@code dataSourceName} and put resource reference in web.xml
     * otherwise {@code dataSourceName} is used as JNDI name.
     *
     * @param jpaProperties the jpa properties
     * @return the data source
     */
    public static DataSource newDataSource(final AbstractJpaProperties jpaProperties) {
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
                LOGGER.warn("Lookup of datasource [{}] failed due to {} "
                        + "falling back to configuration via JPA properties.", dataSourceName, e.getMessage());
            }
        }

        try {
            final HikariDataSource bean = new HikariDataSource();
            if (StringUtils.isNotBlank(jpaProperties.getDriverClass())) {
                bean.setDriverClassName(jpaProperties.getDriverClass());
            }
            bean.setJdbcUrl(jpaProperties.getUrl());
            bean.setUsername(jpaProperties.getUser());
            bean.setPassword(jpaProperties.getPassword());
            bean.setLoginTimeout((int) jpaProperties.getPool().getMaxWait());
            bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
            bean.setMinimumIdle(jpaProperties.getPool().getMinSize());
            bean.setIdleTimeout(jpaProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
            bean.setInitializationFailTimeout(jpaProperties.getFailFastTimeout());
            bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
            bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
            bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
            bean.setAutoCommit(jpaProperties.isAutocommit());
            bean.setValidationTimeout(jpaProperties.getPool().getTimeoutMillis());
            return bean;
        } catch (final Exception e) {
            LOGGER.error("Error creating DataSource: [{}]", e.getMessage());
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
     * New entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newHibernateEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                              final AbstractJpaProperties jpaProperties) {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan().toArray(new String[] {}));

        if (config.getDataSource() != null) {
            bean.setDataSource(config.getDataSource());
        }

        final Properties properties = new Properties();
        properties.put(Environment.DIALECT, jpaProperties.getDialect());
        properties.put(Environment.HBM2DDL_AUTO, jpaProperties.getDdlAuto());
        properties.put(Environment.STATEMENT_BATCH_SIZE, jpaProperties.getBatchSize());
        if (StringUtils.isNotBlank(jpaProperties.getDefaultCatalog())) {
            properties.put(Environment.DEFAULT_CATALOG, jpaProperties.getDefaultCatalog());
        }
        if (StringUtils.isNotBlank(jpaProperties.getDefaultSchema())) {
            properties.put(Environment.DEFAULT_SCHEMA, jpaProperties.getDefaultSchema());
        }
        properties.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, Boolean.TRUE);
        properties.put(Environment.FORMAT_SQL, Boolean.TRUE);
        properties.putAll(jpaProperties.getProperties());
        bean.setJpaProperties(properties);

        return bean;
    }
}
