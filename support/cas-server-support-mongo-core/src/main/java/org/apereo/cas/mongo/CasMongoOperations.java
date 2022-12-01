package org.apereo.cas.mongo;

import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link CasMongoOperations}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface CasMongoOperations extends MongoOperations {
    /**
     * Gets mongo db factory.
     *
     * @return the mongo db factory
     */
    MongoDatabaseFactory getMongoDatabaseFactory();
}
