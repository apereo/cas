package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.zaxxer.hikari.HikariDataSource;
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
import java.util.Properties;
import java.util.UUID;

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
    public DataSource newDataSource(final String driverClass, final String username,
                                    final String password, final String url) {
        return FunctionUtils.doUnchecked(() -> {
            val ds = new SimpleDriverDataSource();
            ds.setDriverClass((Class<Driver>) Class.forName(driverClass));
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setUrl(url);
            return ds;
        });
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
    public CloseableDataSource newDataSource(final AbstractJpaProperties jpaProperties) {
        val dataSourceName = jpaProperties.getDataSourceName();

        if (StringUtils.isNotBlank(dataSourceName)) {
            try {
                val dsLookup = new JndiDataSourceLookup();
                dsLookup.setResourceRef(false);
                val containerDataSource = dsLookup.getDataSource(dataSourceName);
                return new DefaultCloseableDataSource(containerDataSource);
            } catch (final DataSourceLookupFailureException e) {
                LOGGER.warn("Lookup of datasource [{}] failed due to [{}]. Back to JPA properties.", dataSourceName, e.getMessage());
            }
        }

        val bean = new HikariDataSource();
        FunctionUtils.doIfNotBlank(jpaProperties.getDriverClass(), __ -> bean.setDriverClassName(jpaProperties.getDriverClass()));

        val url = SpringExpressionLanguageValueResolver.getInstance().resolve(jpaProperties.getUrl());
        bean.setJdbcUrl(url);
        bean.setUsername(jpaProperties.getUser());
        bean.setPassword(jpaProperties.getPassword());

        val poolSettings = jpaProperties.getPool();
        FunctionUtils.doUnchecked(__ -> bean.setLoginTimeout((int) Beans.newDuration(poolSettings.getMaxWait()).toSeconds()));
        bean.setMaximumPoolSize(poolSettings.getMaxSize());
        bean.setMinimumIdle(poolSettings.getMinSize());
        bean.setIdleTimeout(Beans.newDuration(jpaProperties.getIdleTimeout()).toMillis());
        bean.setLeakDetectionThreshold(Beans.newDuration(jpaProperties.getLeakThreshold()).toMillis());
        bean.setInitializationFailTimeout(jpaProperties.getFailFastTimeout());
        bean.setIsolateInternalQueries(jpaProperties.isIsolateInternalQueries());
        bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
        bean.setAllowPoolSuspension(poolSettings.isSuspension());
        bean.setAutoCommit(jpaProperties.isAutocommit());
        bean.setValidationTimeout(poolSettings.getTimeoutMillis());
        bean.setReadOnly(jpaProperties.isReadOnly());
        bean.setPoolName(StringUtils.defaultIfBlank(poolSettings.getName(), UUID.randomUUID().toString()));
        bean.setKeepaliveTime(Beans.newDuration(poolSettings.getKeepAliveTime()).toMillis());
        bean.setMaxLifetime(Beans.newDuration(poolSettings.getMaximumLifetime()).toMillis());
        bean.setSchema(jpaProperties.getDefaultSchema());
        bean.setConnectionTimeout(Beans.newDuration(jpaProperties.getConnectionTimeout()).toMillis());

        val dataSourceProperties = new Properties();
        dataSourceProperties.putAll(jpaProperties.getProperties());
        bean.setDataSourceProperties(dataSourceProperties);
        
        return new DefaultCloseableDataSource(bean);
    }

    /**
     * New entity manager factory bean.
     *
     * @param config the config
     * @return the local container entity manager factory bean
     */
    public LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(final JpaConfigurationContext config) {
        val bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (config.getPersistenceProvider() != null) {
            bean.setPersistenceProvider(config.getPersistenceProvider());
        }

        FunctionUtils.doIfNotBlank(config.getPersistenceUnitName(), __ -> bean.setPersistenceUnitName(config.getPersistenceUnitName()));
        if (!config.getPackagesToScan().isEmpty()) {
            bean.setPackagesToScan(config.getPackagesToScan().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }
        if (config.getDataSource() != null) {
            bean.setDataSource(config.getDataSource());
        }
        bean.getJpaPropertyMap().putAll(config.getJpaProperties());
        return bean;
    }

    /**
     * Is valid data source connection.
     *
     * @param ds      the ds
     * @param timeout the timeout
     * @return true/false
     */
    public boolean isValidDataSourceConnection(final CloseableDataSource ds, final int timeout) {
        try (val con = ds.getConnection()) {
            return con.isValid(timeout);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
