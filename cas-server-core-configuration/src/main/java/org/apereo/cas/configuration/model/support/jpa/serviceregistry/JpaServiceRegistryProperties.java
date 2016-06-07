package org.apereo.cas.configuration.model.support.jpa.serviceregistry;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for svcreg.database.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "svcreg.database", ignoreUnknownFields = false)
public class JpaServiceRegistryProperties extends AbstractJpaProperties {

    public JpaServiceRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-service-registry");
    }
}
