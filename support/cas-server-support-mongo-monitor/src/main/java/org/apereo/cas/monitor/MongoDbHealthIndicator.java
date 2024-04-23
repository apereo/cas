package org.apereo.cas.monitor;

import org.apereo.cas.mongo.CasMongoOperations;

import lombok.val;
import org.bson.Document;

/**
 * This is {@link MongoDbHealthIndicator} where it attempts to collect statistics
 * on all mongodb instances configured inside CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbHealthIndicator extends AbstractCacheHealthIndicator {
    private final CasMongoOperations mongoTemplate;

    public MongoDbHealthIndicator(final CasMongoOperations mongoTemplate,
                                  final long evictionThreshold,
                                  final long threshold) {
        super(evictionThreshold, threshold);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        val list = mongoTemplate.getCollectionNames()
            .stream()
            .map(c -> {
                val stats = mongoTemplate.executeCommand(new Document("collStats", c));
                return new MongoDbCacheStatistics(stats, c);
            }).toList();

        return list.toArray(CacheStatistics[]::new);
    }

    @Override
    protected String getName() {
        val dbName = mongoTemplate.getMongoDatabaseFactory().getMongoDatabase().getName();
        return super.getName() + '-' + dbName;
    }
}
