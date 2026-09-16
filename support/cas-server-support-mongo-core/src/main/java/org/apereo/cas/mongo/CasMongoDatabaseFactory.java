package org.apereo.cas.mongo;

import module java.base;
import com.mongodb.client.MongoClient;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * This is {@link CasMongoDatabaseFactory}, a database factory that owns the client it is
 * handed and closes it when the factory is disposed of.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMongoDatabaseFactory extends SimpleMongoClientDatabaseFactory {
    private final MongoClient managedClient;

    public CasMongoDatabaseFactory(final MongoClient mongoClient, final String databaseName) {
        super(mongoClient, databaseName);
        this.managedClient = mongoClient;
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        managedClient.close();
    }
}
