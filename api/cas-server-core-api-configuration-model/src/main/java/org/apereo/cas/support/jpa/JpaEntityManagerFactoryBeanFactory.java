package org.apereo.cas.support.jpa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;

/**
 * This this {@link JpaEntityManagerFactoryBeanFactory}. Decouple EntityManagerFactory from core functionality to allow JPA provider independence.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@FunctionalInterface
public interface JpaEntityManagerFactoryBeanFactory {

    /**
     * Creates and configures the correct {@link EntityManagerFactory} for a given JPA connection.
     * @param config JPA configuration data holder
     * @param jpaProperties runtime properties
     * @return EntityManagerFactoryBean fully configured
     */
    LocalContainerEntityManagerFactoryBean newEntityManagerFactoryBean(JpaConfigDataHolder config, AbstractJpaProperties jpaProperties);
}
