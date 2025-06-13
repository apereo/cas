package org.apereo.cas.eclipselink;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import lombok.val;
import org.eclipse.persistence.config.BatchWriting;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.spi.PersistenceProvider;
import java.io.Serializable;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * This is {@link CasEclipseLinkJpaBeanFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class CasEclipseLinkJpaBeanFactory implements JpaBeanFactory {

    @Override
    public JpaVendorAdapter newJpaVendorAdapter(final DatabaseProperties properties) {
        val adapter = new EclipseLinkJpaVendorAdapter();
        adapter.setGenerateDdl(properties.isGenDdl());
        adapter.setShowSql(properties.isShowSql());
        return adapter;
    }

    @Override
    public FactoryBean<EntityManagerFactory> newEntityManagerFactoryBean(final JpaConfigurationContext config,
                                                                         final AbstractJpaProperties jpaProperties) {
        val bean = JpaBeans.newEntityManagerFactoryBean(config);

        val map = new HashMap<String, Object>();
        map.put(PersistenceUnitProperties.WEAVING, Boolean.FALSE.toString());
        map.put(PersistenceUnitProperties.DDL_GENERATION, jpaProperties.getDdlAuto());
        map.put(PersistenceUnitProperties.BATCH_WRITING_SIZE, jpaProperties.getBatchSize());
        map.put(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
        map.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.FINE_LABEL);
        bean.getJpaPropertyMap().putAll(map);
        bean.afterPropertiesSet();
        return bean;
    }

    @Override
    public Stream<? extends Serializable> streamQuery(final Query query) {
        return query.getResultStream();
    }

    @Override
    public PersistenceProvider newPersistenceProvider(final AbstractJpaProperties jpa) {
        return new org.eclipse.persistence.jpa.PersistenceProvider();
    }
}
