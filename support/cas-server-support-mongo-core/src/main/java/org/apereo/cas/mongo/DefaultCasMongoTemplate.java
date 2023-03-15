package org.apereo.cas.mongo;

import lombok.val;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    @Override
    protected <T> Stream<T> doStream(final Query query, final Class<?> entityType, final String collectionName, final Class<T> returnType) {
        try (val stream = super.doStream(query, entityType, collectionName, returnType)){
            return stream.collect(Collectors.toList()).stream();
        }
    }
}
