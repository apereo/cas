package org.apereo.cas.configuration.model.support.jpa.serviceregistry;

import module java.base;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

/**
 * Configuration properties class for JPA service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-jpa-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class JpaServiceRegistryProperties extends AbstractJpaProperties {

    @Serial
    private static final long serialVersionUID = 352435146313504995L;

    /**
     * Whether managing services via JPA is enabled.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * The execution order of this registry
     * which will determine its position in a chain
     * in case multiple registries are defined.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;
    
    public JpaServiceRegistryProperties() {
        setUrl("jdbc:hsqldb:mem:cas-service-registry");
    }
}
