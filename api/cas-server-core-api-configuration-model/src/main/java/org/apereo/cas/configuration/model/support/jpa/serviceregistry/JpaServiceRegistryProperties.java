package org.apereo.cas.configuration.model.support.jpa.serviceregistry;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

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

    private static final long serialVersionUID = 352435146313504995L;

    /**
     * List of class names, fully qualified, that
     * should be managed by the JPA persistence unit
     * in addition to what CAS may discover dynamically
     * from various extensions at runtime.
     */
    private List<String> managedEntities = new ArrayList<>();

    public JpaServiceRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-service-registry");
    }
}
