package org.apereo.cas.mongo;

import module java.base;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * This is {@link DefaultCasMongoTemplate}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DefaultCasMongoTemplate extends MongoTemplate implements CasMongoOperations {
    public DefaultCasMongoTemplate(final MongoDatabaseFactory mongoDbFactory,
                                   final MappingMongoConverter mappingMongoConverter) {
        super(mongoDbFactory, mappingMongoConverter);
    }
}
