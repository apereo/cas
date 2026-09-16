package org.apereo.cas.mongo;

import module java.base;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * This is {@link DefaultCasMongoTemplate}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DefaultCasMongoTemplate extends MongoTemplate implements CasMongoOperations, DisposableBean {
    public DefaultCasMongoTemplate(final MongoDatabaseFactory mongoDbFactory,
                                   final MappingMongoConverter mappingMongoConverter) {
        super(mongoDbFactory, mappingMongoConverter);
    }

    @Override
    public void destroy() throws Exception {
        if (getMongoDatabaseFactory() instanceof final DisposableBean disposableBean) {
            disposableBean.destroy();
        }
    }
}
