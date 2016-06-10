package org.apereo.cas.configuration.model.support.jpa.serviceregistry;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

/**
 * Configuration properties class for svcreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class JpaServiceRegistryProperties extends AbstractJpaProperties {

    public JpaServiceRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-service-registry");
    }
}
