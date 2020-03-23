package org.apereo.cas.configuration.model.support.couchdb.serviceregistry;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchDbServiceRegistryProperties}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-service-registry")
@Accessors(chain = true)
@Getter
@Setter
public class CouchDbServiceRegistryProperties extends BaseCouchDbProperties {
    private static final long serialVersionUID = -5101551655756163621L;

    public CouchDbServiceRegistryProperties() {
        this.setDbName("service_registry");
    }
}
