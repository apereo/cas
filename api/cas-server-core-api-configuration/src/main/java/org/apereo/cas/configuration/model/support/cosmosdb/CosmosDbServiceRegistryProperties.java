package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CosmosDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-service-registry")
public class CosmosDbServiceRegistryProperties extends BaseCosmosDbProperties implements Serializable {
    private static final long serialVersionUID = 6194689836396653458L;

    /**
     * Collection to store CAS service definitions.
     */
    @RequiredProperty
    private String collection = "CasCosmosDbServiceRegistry";

    public String getCollection() {
        return collection;
    }

    public void setCollection(final String collection) {
        this.collection = collection;
    }
}
