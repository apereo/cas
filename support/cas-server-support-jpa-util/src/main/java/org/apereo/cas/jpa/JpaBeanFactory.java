package org.apereo.cas.jpa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;

import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.jpa.JpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.spi.PersistenceProvider;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * This is {@link JpaBeanFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface JpaBeanFactory {

    /**
     * Default implementation bean.
     */
    String DEFAULT_BEAN_NAME = "jpaBeanFactory";

    /**
     * New jpa vendor adapter jpa vendor adapter.
     *
     * @param properties the properties
     * @return the jpa vendor adapter
     */
    JpaVendorAdapter newJpaVendorAdapter(DatabaseProperties properties);

    /**
     * New jpa vendor adapter.
     *
     * @return the jpa vendor adapter
     */
    default JpaVendorAdapter newJpaVendorAdapter() {
        val properties = new DatabaseProperties();
        properties.setGenDdl(true);
        properties.setShowSql(true);
        return newJpaVendorAdapter(properties);
    }

    /**
     * New entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    FactoryBean<EntityManagerFactory> newEntityManagerFactoryBean(JpaConfigurationContext config,
                                                                  AbstractJpaProperties jpaProperties);

    /**
     * New persistence provider.
     *
     * @param jpa the jpa
     * @return the persistence provider
     */
    PersistenceProvider newPersistenceProvider(AbstractJpaProperties jpa);

    /**
     * Stream query.
     *
     * @param query the query
     * @return the stream
     */
    Stream<? extends Serializable> streamQuery(Query query);
}
