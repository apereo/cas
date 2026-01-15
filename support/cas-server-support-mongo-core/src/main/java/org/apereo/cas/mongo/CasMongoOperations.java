package org.apereo.cas.mongo;

import module java.base;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

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

    /**
     * Cast to mongo template.
     *
     * @return the mongo template
     */
    default MongoTemplate asMongoTemplate() {
        return (MongoTemplate) this;
    }
}
