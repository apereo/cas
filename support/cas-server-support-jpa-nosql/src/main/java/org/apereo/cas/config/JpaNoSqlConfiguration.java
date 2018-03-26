package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.jpa.DefaultJpaStreamerFactory;
import org.apereo.cas.ticket.Ticket;
import org.hibernate.LockOptions;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.jpa.HibernateOgmPersistence;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Properties;

import static org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties.JpaType.*;
import static org.apereo.cas.configuration.support.JpaBeans.*;

/**
 * Configuration for core JPA interface components.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("jpaNoSqlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
@AutoConfigureBefore(JpaCoreConfiguration.class)
public class JpaNoSqlConfiguration {

    /**
     * Batch size for streaming queries.
     */
    public static final int STREAM_BATCH_SIZE = 100;

    @ConditionalOnMissingBean(name = "noSqlJpaEntityManagerFactoryBeanFactoryConfigurer")
    @Bean
    @Autowired
    public JpaEntityManagerFactoryBeanFactoryConfigurer noSqlJpaEntityManagerFactoryBeanFactoryConfigurer() {
        return (DefaultJpaEntityManagerFactoryBeanFactory factory) -> {
            factory.registerJpaEntityManagerFactoryBeanFactory(NOSQL, (config, jpaProperties) -> {
                final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

                if (StringUtils.isNotBlank(config.getPersistenceUnitName())) {
                    bean.setPersistenceUnitName(config.getPersistenceUnitName());
                }
                bean.setPersistenceProviderClass(HibernateOgmPersistence.class);
                bean.setPackagesToScan(config.getPackagesToScan().toArray(new String[] {}));


                final Properties properties = new Properties();

                properties.put(OgmProperties.DATASTORE_PROVIDER, jpaProperties.getProvider());
                putPropertyUnlessBlank(properties, OgmProperties.HOST, jpaProperties.getHost());
                putPropertyUnlessBlank(properties, OgmProperties.USERNAME, jpaProperties.getUser());
                putPropertyUnlessBlank(properties, OgmProperties.PASSWORD, jpaProperties.getPassword());
                putPropertyUnlessBlank(properties, OgmProperties.DATABASE, jpaProperties.getDatabase());
                properties.put(OgmProperties.CREATE_DATABASE, jpaProperties.getCreateDatabase());
                properties.putAll(jpaProperties.getProperties());
                bean.setJpaProperties(properties);

                return bean;
            });
        };
    }

    @ConditionalOnMissingBean(name = "noSqlJpaStreamerConfigurer")
    @Bean
    public JpaStreamerFactoryConfigurer noSqlJpaStreamerConfigurer() {
        return (DefaultJpaStreamerFactory factory) -> {
            factory.registerJpaStreamer(NOSQL, (TypedQuery<? extends Ticket> query) -> {
                final Query<Ticket> hq = (Query<Ticket>) query.unwrap(Query.class);
                hq.setFetchSize(STREAM_BATCH_SIZE);
                hq.setLockOptions(LockOptions.NONE);
                return hq.stream();
            });
        };
    }

    @ConditionalOnMissingBean(name = "jpaStreamerFactory")
    @Autowired
    @Bean
    public DefaultJpaStreamerFactory jpaStreamerFactory(final List<JpaStreamerFactoryConfigurer> configurers) {
        final DefaultJpaStreamerFactory factory = new DefaultJpaStreamerFactory();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring JpaStreamerFactory [{}]", name);
            c.configureDefaultJpaStreamerFactory(factory);
        });
        return factory;
    }
}
