package org.apereo.cas.monitor;

import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.StringWriter;

/**
 * This is {@link MongoDbCacheStatistics}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
public class MongoDbCacheStatistics implements CacheStatistics {
    private final DBCollection collection;
    private final CommandResult statistics;

    public MongoDbCacheStatistics(final DBCollection collection) {
        this.collection = collection;
        this.statistics = collection.getStats();
    }

    @Override
    public long getSize() {
        return statistics.getLong("size");
    }

    @Override
    public long getCapacity() {
        return statistics.getLong("storageSize");
    }

    @Override
    public long getPercentFree() {
        return getCapacity() - statistics.getLong("totalIndexSize");
    }

    @Override
    public String getName() {
        return this.collection.getName();
    }

    @Override
    public String toString(final StringBuilder builder) {
        try {
            final var json = JsonValue.readJSON(this.statistics.toString());
            final var writer = new StringWriter();
            json.writeTo(writer, Stringify.FORMATTED);
            builder.append(writer.toString());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return builder.toString();
    }
}
