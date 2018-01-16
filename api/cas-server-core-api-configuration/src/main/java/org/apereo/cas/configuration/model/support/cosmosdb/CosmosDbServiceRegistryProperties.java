package org.apereo.cas.configuration.model.support.cosmosdb;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CosmosDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-service-registry")
@Slf4j
@Getter
@Setter
public class CosmosDbServiceRegistryProperties extends BaseCosmosDbProperties implements Serializable {

    private static final long serialVersionUID = 6194689836396653458L;

    /**
     * Collection to store CAS service definitions.
     */
    @RequiredProperty
    private String collection = "CasCosmosDbServiceRegistry";
}
