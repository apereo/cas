package org.apereo.cas.configuration.model.support.couchdb;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CouchDbServiceRegistryProperties}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-couchdb-service-registry")
@Getter
@Setter
public class CouchDbServiceRegistryProperties extends AbstractCouchDbProperties {
    private static final long serialVersionUID = -5101551655756163621L;

    public CouchDbServiceRegistryProperties() {
        this.setDbName("serviceRegistry");
    }
}
