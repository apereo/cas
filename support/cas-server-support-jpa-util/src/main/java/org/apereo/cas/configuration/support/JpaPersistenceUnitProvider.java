package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * This is {@link JpaPersistenceUnitProvider}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface JpaPersistenceUnitProvider extends DisposableBean {

    /**
     * Gets application context.
     *
     * @return the application context
     */
    ConfigurableApplicationContext getApplicationContext();

    /**
     * Gets default/fallback entity manager.
     *
     * @return the entity manager
     */
    EntityManager getEntityManager();

    /**
     * Create entity manager.
     *
     * @return the entity manager
     */
    default EntityManager recreateEntityManagerIfNecessary(final String persistenceUnitName) {
        val currentEntityManager = getEntityManager();
        return FunctionUtils.doIf(currentEntityManager == null && CasRuntimeHintsRegistrar.inNativeImage(), () -> {
            val beanFactory = getApplicationContext().getBeanFactory();
            val entityManagerFactory = Arrays.stream(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, EntityManagerFactory.class))
                .map(beanFactory::getBean)
                .filter(EntityManagerFactoryInfo.class::isInstance)
                .map(EntityManagerFactoryInfo.class::cast)
                .filter(factory -> persistenceUnitName.equals(factory.getPersistenceUnitName()))
                .map(EntityManagerFactory.class::cast)
                .findFirst()
                .orElseGet(() -> beanFactory.getBean(persistenceUnitName, EntityManagerFactory.class));
            return entityManagerFactory.createEntityManager();
        }, () -> currentEntityManager).get();
    }

    @Override
    default void destroy() {
        FunctionUtils.doAndHandle(_ -> getEntityManager().close());
    }
}
