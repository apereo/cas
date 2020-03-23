package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    private static final long serialVersionUID = -227092724742371662L;

    public MongoDbServiceRegistryProperties() {
        setCollection("cas-service-registry");
    }
}
