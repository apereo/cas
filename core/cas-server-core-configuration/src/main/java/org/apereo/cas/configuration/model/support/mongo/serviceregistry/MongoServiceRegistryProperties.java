package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class MongoServiceRegistryProperties extends AbstractMongoInstanceProperties {
    public MongoServiceRegistryProperties() {
        setCollectionName("cas-service-registry");
    }
}
