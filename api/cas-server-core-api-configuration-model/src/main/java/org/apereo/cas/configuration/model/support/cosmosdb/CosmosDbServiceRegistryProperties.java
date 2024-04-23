package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CosmosDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class CosmosDbServiceRegistryProperties extends BaseCosmosDbProperties {

    @Serial
    private static final long serialVersionUID = 6194689836396653458L;

    /**
     * Collection to store CAS service definitions.
     */
    @RequiredProperty
    private String container = "CasCosmosDbServiceRegistry";

    /**
     * Whether collections should be created on startup.
     */
    private boolean createContainer;
}
