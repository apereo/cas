package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

import java.sql.Driver;

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
     * New simple data source.
     *
     * @param driverClass the driver class
     * @param username    the username
     * @param password    the password
     * @param url         the url
     * @return the data source
     */
    @SneakyThrows
    public static DataSource newDataSource(final String driverClass, final String username,
                                           final String password, final String url) {
        val ds = new SimpleDriverDataSource();
        ds.setDriverClass((Class<Driver>) Class.forName(driverClass));
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setUrl(url);
        return ds;
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
    public static DataSource newDataSource(final AbstractJpaProperties jpaProperties) {
        val dataSourceName = jpaProperties.getDataSourceName();

        if (StringUtils.isNotBlank(dataSourceName)) {
            val proxyDataSource = jpaProperties.isDataSourceProxy();
            try {
                val dsLookup = new JndiDataSourceLookup();
                dsLookup.setResourceRef(false);
                val containerDataSource = dsLookup.getDataSource(dataSourceName);
                if (!proxyDataSource) {
                    return containerDataSource;
                }
                return new DataSourceProxy(containerDataSource);
            } catch (final DataSourceLookupFailureException e) {
                LOGGER.warn("Lookup of datasource [{}] failed due to [{}] falling back to configuration via JPA properties.", dataSourceName, e.getMessage());
            }
        }

        val bean = new HikariDataSource();
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
     * New entity manager factory bean.
     *
     * @param config the config
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(final JpaConfigurationContext config) {
        val bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        if (config.getDataSource() != null) {
            bean.setDataSource(config.getDataSource());
        }
        bean.getJpaPropertyMap().putAll(config.getJpaProperties());
        return bean;
    }
}
