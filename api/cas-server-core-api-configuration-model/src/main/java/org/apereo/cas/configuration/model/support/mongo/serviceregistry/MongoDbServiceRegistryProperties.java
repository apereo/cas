package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-mongo-service-registry")
@Slf4j
@Getter
@Setter
public class MongoDbServiceRegistryProperties extends SingleCollectionMongoDbProperties {

    private static final long serialVersionUID = -227092724742371662L;

    public MongoDbServiceRegistryProperties() {
        setCollection("cas-service-registry");
    }
}
