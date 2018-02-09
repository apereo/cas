package org.apereo.cas.config;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.support.jpa.JpaEntityManagerFactoryBeanFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decouples EntityManagerFactory creation from JPA core functionality to allow multiple JPA implementations.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
public class DefaultJpaEntityManagerFactoryBeanFactory {
    private final Map<AbstractJpaProperties.JpaType, JpaEntityManagerFactoryBeanFactory> jpaTypeMap;

    public DefaultJpaEntityManagerFactoryBeanFactory() {
        jpaTypeMap = new ConcurrentHashMap<>();
    }

    /**
     * Register a EnitityManagerFactory.
     * @param jpaType JPA type to register for
     * @param factory Factory to register
     */
    public void registerJpaEntityManagerFactoryBeanFactory(final AbstractJpaProperties.JpaType jpaType, final JpaEntityManagerFactoryBeanFactory factory) {
        jpaTypeMap.put(jpaType, factory);
    }

    /**
     * Gets the correct EntityManagerFactory for a given JPA type.
     * @param type JPA type to get entity manager for.
     * @return Entity manager factory.
     */
    public JpaEntityManagerFactoryBeanFactory getFactoryForType(final AbstractJpaProperties.JpaType type) {
        return jpaTypeMap.get(type);
    }
}
