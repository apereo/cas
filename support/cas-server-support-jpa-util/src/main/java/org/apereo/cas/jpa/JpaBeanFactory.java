package org.apereo.cas.jpa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;

import lombok.val;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * This is {@link JpaBeanFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface JpaBeanFactory {

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
     * New entity manager factory bean local container entity manager factory bean.
     *
     * @param config        the config
     * @param jpaProperties the jpa properties
     * @return the local container entity manager factory bean
     */
    LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(JpaConfigurationContext config,
                                                                       AbstractJpaProperties jpaProperties);
}
