package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class MongoServiceRegistryProperties extends AbstractMongoInstanceProperties {
    private static final long serialVersionUID = -227092724742371662L;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    /**
     * MongoDb database collection name to fetch and/or create.
     */
    private String collectionName;
    
    public MongoServiceRegistryProperties() {
        setCollectionName("cas-service-registry");
    }

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }

}
