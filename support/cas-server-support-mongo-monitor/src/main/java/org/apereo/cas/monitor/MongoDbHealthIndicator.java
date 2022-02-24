package org.apereo.cas.monitor;

import lombok.val;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.stream.Collectors;

/**
 * This is {@link MongoDbHealthIndicator} where it attempts to collect statistics
 * on all mongodb instances configured inside CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbHealthIndicator extends AbstractCacheHealthIndicator {
    private final MongoOperations mongoTemplate;

    public MongoDbHealthIndicator(final MongoOperations mongoTemplate,
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
            })
            .collect(Collectors.toList());

        return list.toArray(CacheStatistics[]::new);
    }

    @Override
    protected String getName() {
        val dbName = String.join(",", mongoTemplate.getCollectionNames());
        return super.getName() + '-' + dbName;
    }
}
