package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.support.jpa.DefaultJpaStreamerFactory;
import org.apereo.cas.ticket.Ticket;
import org.hibernate.LockOptions;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.TypedQuery;
import java.util.Properties;

import static org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties.JpaType.*;

/**
 * Configuration for JDBC JPA implementation components.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Configuration("jpaJdbcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class JpaJdbcConfiguration {
    /**
     * Batch size for streaming queries.
     */
    public static final int STREAM_BATCH_SIZE = 100;

    @ConditionalOnMissingBean(name = "hibernateJpaEntityManagerFactoryBeanFactoryConfigurer")
    @Bean
    public JpaEntityManagerFactoryBeanFactoryConfigurer hibernateJpaEntityManagerFactoryBeanFactoryConfigurer() {
        return (DefaultJpaEntityManagerFactoryBeanFactory factory) -> {
            factory.registerJpaEntityManagerFactoryBeanFactory(JDBC, (config, jpaProperties) -> {
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
                properties.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, Boolean.TRUE);
                properties.put(Environment.FORMAT_SQL, Boolean.TRUE);

                JpaBeans.putPropertyUnlessBlank(properties, Environment.DEFAULT_CATALOG, jpaProperties.getDefaultCatalog());
                JpaBeans.putPropertyUnlessBlank(properties, Environment.DEFAULT_SCHEMA, jpaProperties.getDefaultSchema());

                properties.putAll(jpaProperties.getProperties());
                bean.setJpaProperties(properties);

                return bean;
            });
        };
    }

    @ConditionalOnMissingBean(name = "hibernateJpaStreamerConfigurer")
    @Bean
    public JpaStreamerFactoryConfigurer hibernateJpaStreamerConfigurer() {
        return (DefaultJpaStreamerFactory factory) -> {
            factory.registerJpaStreamer(JDBC, (TypedQuery<? extends Ticket> query) -> {
                final Query<Ticket> hq = (Query<Ticket>) query.unwrap(Query.class);
                hq.setFetchSize(STREAM_BATCH_SIZE);
                hq.setLockOptions(LockOptions.NONE);
                return hq.stream();
            });
        };
    }

}
