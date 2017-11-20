package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo-service-registry")
public class MongoServiceRegistryProperties extends SingleCollectionMongoDbProperties {
    private static final long serialVersionUID = -227092724742371662L;
    
    public MongoServiceRegistryProperties() {
        setCollection("cas-service-registry");
    }
}
