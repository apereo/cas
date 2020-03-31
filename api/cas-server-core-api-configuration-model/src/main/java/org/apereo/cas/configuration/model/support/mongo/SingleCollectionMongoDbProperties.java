package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SingleCollectionMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-mongo-core")
@Accessors(chain = true)
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
}
