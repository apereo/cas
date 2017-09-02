package org.apereo.cas.monitor;

import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * This is {@link MongoDbCacheStatistics}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbCacheStatistics implements CacheStatistics {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbCacheStatistics.class);

    private final DBCollection collection;
    private final CommandResult statistics;


    public MongoDbCacheStatistics(final DBCollection collection) {
        this.collection = collection;
        this.statistics = collection.getStats();
    }

    @Override
    public long getSize() {
        return statistics.getLong("objects");
    }

    @Override
    public long getCapacity() {
        return statistics.getLong("storageSize");
    }

    @Override
    public String getName() {
        return this.collection.getName();
    }

    @Override
    public void toString(final StringBuilder builder) {
        try {
            final JsonValue json = JsonValue.readJSON(this.statistics.toString());
            final StringWriter writer = new StringWriter();
            json.writeTo(writer, Stringify.FORMATTED);
            builder.append(writer.toString());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        this.toString(builder);
        return builder.toString();
    }
}
