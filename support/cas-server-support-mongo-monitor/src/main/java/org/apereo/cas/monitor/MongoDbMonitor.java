package org.apereo.cas.monitor;

import com.mongodb.DBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbMonitor} where it attempts to collect statistics
 * on all mongodb instances configured inside CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbMonitor extends AbstractCacheMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbMonitor.class);
    private final MongoTemplate mongoTemplate;

    public MongoDbMonitor(final MongoTemplate mongoTemplate) {
        super(MongoDbMonitor.class.getSimpleName());
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> list = mongoTemplate.getCollectionNames()
                .stream()
                .map(c -> {
                    final DBCollection col = mongoTemplate.getCollection(c);
                    return new MongoDbCacheStatistics(col);
                })
                .collect(Collectors.toList());

        return list.toArray(new CacheStatistics[]{});
    }
}
