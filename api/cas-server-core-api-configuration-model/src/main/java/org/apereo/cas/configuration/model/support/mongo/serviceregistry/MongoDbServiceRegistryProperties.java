package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import module java.base;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbServiceRegistryProperties extends SingleCollectionMongoDbProperties {

    @Serial
    private static final long serialVersionUID = -227092724742371662L;

    /**
     * The execution order of this registry
     * which will determine its position in a chain
     * in case multiple registries are defined.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;
    
    public MongoDbServiceRegistryProperties() {
        setCollection("cas-service-registry");
    }
}
