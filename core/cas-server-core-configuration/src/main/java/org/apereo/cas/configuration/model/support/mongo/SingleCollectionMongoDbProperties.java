package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link SingleCollectionMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SingleCollectionMongoDbProperties extends BaseMongoDbProperties {
    private static final long serialVersionUID = 4869686250345657447L;

    /**
     * MongoDb database collection name to fetch and/or create.
     */
    @RequiredProperty
    private String collection;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }
    
    public String getCollection() {
        return collection;
    }

    public void setCollection(final String collection) {
        this.collection = collection;
    }
}
