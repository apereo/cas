package org.apereo.cas.configuration.model.support.couchdb.serviceregistry;

import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link CouchDbServiceRegistryProperties}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-service-registry")
public class CouchDbServiceRegistryProperties extends BaseCouchDbProperties {
    private static final long serialVersionUID = -5101551655756163621L;

    public CouchDbServiceRegistryProperties() {
        this.setDbName("service_registry");
    }
}
