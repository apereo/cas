package org.apereo.cas.hibernate;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * This is {@link CasHibernateJpaBeanFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasHibernateJpaBeanFactory implements JpaBeanFactory {

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public JpaVendorAdapter newJpaVendorAdapter(final DatabaseProperties databaseProperties) {
        val bean = new HibernateJpaVendorAdapter();
        bean.setGenerateDdl(databaseProperties.isGenDdl());
        bean.setShowSql(databaseProperties.isShowSql());
        return bean;
    }

    @Override
    public LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(final JpaConfigurationContext config,
                                                                              final AbstractJpaProperties jpaProperties) {
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
        properties.put(Environment.AUTOCOMMIT, jpaProperties.isAutocommit());
        properties.put("hibernate.jdbc.time_zone", "UTC");
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

        val bean = JpaBeans.newEntityManagerFactoryBean(config);
        bean.setJpaProperties(properties);
        return bean;
    }

}
