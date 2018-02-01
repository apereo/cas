package org.apereo.cas.configuration.model.support.mongo;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link SingleCollectionMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
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
