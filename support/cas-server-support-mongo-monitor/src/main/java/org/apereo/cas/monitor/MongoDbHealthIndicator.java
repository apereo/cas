package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbHealthIndicator} where it attempts to collect statistics
 * on all mongodb instances configured inside CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MongoDbHealthIndicator extends AbstractCacheHealthIndicator {
    private final transient MongoTemplate mongoTemplate;

    public MongoDbHealthIndicator(final MongoTemplate mongoTemplate,
                                  final long evictionThreshold, final long threshold) {
        super(evictionThreshold, threshold);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> list = mongoTemplate.getCollectionNames()
            .stream()
            .map(c -> {
                final var col = this.mongoTemplate.getMongoDbFactory().getLegacyDb().getCollection(c);
                return new MongoDbCacheStatistics(col);
            })
            .collect(Collectors.toList());

        return list.toArray(new CacheStatistics[]{});
    }
}
