package org.apereo.cas.hibernate;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * This is {@link HibernateBeans}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
@Slf4j
public class HibernateBeans {

    /**
     * New hibernate jpa vendor adapter.
     *
     * @param databaseProperties the database properties
     * @return the hibernate jpa vendor adapter
     */
    public static HibernateJpaVendorAdapter newHibernateJpaVendorAdapter(final DatabaseProperties databaseProperties) {
        val bean = new HibernateJpaVendorAdapter();
        bean.setGenerateDdl(databaseProperties.isGenDdl());
        bean.setShowSql(databaseProperties.isShowSql());
        return bean;
    }


    /**
     * New entity manager factory bean.
     *
     * @param config             the config
     * @param jpaProperties      the jpa properties
     * @param applicationContext the application context
     * @return the local container entity manager factory bean
     */
    public static LocalContainerEntityManagerFactoryBean newHibernateEntityManagerFactoryBean(final JpaConfigDataHolder config,
                                                                                              final AbstractJpaProperties jpaProperties,
                                                                                              final ApplicationContext applicationContext) {
        val bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(config.getJpaVendorAdapter());

        if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
            bean.setPersistenceUnitName(config.getPersistenceUnitName());
        }
        bean.setPackagesToScan(config.getPackagesToScan().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        if (config.getDataSource() != null) {
            bean.setDataSource(config.getDataSource());
        }

        val properties = new Properties();
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
        properties.put("hibernate.connection.useUnicode", Boolean.TRUE);
        properties.put("hibernate.connection.characterEncoding", StandardCharsets.UTF_8.name());
        properties.put("hibernate.connection.charSet", StandardCharsets.UTF_8.name());
        if (StringUtils.isNotBlank(jpaProperties.getPhysicalNamingStrategyClassName())) {
            try {
                val clazz = ClassUtils.getClass(JpaBeans.class.getClassLoader(), jpaProperties.getPhysicalNamingStrategyClassName());
                val namingStrategy = PhysicalNamingStrategy.class.cast(clazz.getDeclaredConstructor().newInstance());
                if (namingStrategy instanceof ApplicationContextAware) {
                    ((ApplicationContextAware) namingStrategy).setApplicationContext(applicationContext);
                }
                properties.put(Environment.PHYSICAL_NAMING_STRATEGY, namingStrategy);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        properties.putAll(jpaProperties.getProperties());
        bean.setJpaProperties(properties);
        return bean;
    }

}
