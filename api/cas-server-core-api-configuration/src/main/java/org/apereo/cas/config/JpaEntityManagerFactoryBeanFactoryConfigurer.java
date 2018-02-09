package org.apereo.cas.config;

/**
 * Factories for creating EntityBeanManagerFactories. This component provides a way to inject stateless factories into components that produce stateful
 * JPA EntityManager instances to decouple the JPA implementation from core so other JPA implementations may be used without adding the dependency to core.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@FunctionalInterface
public interface JpaEntityManagerFactoryBeanFactoryConfigurer {

    /**
     * Callback to configure a factory.
     * @param factory to configure
     */
    void configureDefaultJpaEntityManagerFactoryBeanFactory(DefaultJpaEntityManagerFactoryBeanFactory factory);
}
