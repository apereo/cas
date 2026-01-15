package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import jakarta.persistence.EntityManager;

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
            val entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(getApplicationContext().getBeanFactory(), persistenceUnitName);
            return entityManagerFactory.createEntityManager();
        }, () -> currentEntityManager).get();
    }

    @Override
    default void destroy() {
        FunctionUtils.doAndHandle(_ -> getEntityManager().close());
    }
}
