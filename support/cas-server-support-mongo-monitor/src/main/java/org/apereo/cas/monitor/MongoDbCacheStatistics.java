package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
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
@RequiredArgsConstructor
public class MongoDbCacheStatistics implements CacheStatistics {

    private final Document statistics;

    private final String collectionName;

    @Override
    public long getSize() {
        return statistics.getInteger("size");
    }

    @Override
    public long getCapacity() {
        return statistics.getInteger("storageSize");
    }

    @Override
    public long getPercentFree() {
        return getCapacity() - statistics.getInteger("totalIndexSize");
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public String toString(final StringBuilder builder) {
        try {
            val json = JsonValue.readJSON(this.statistics.toString());
            val writer = new StringWriter();
            json.writeTo(writer, Stringify.FORMATTED);
            builder.append(writer.toString());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return builder.toString();
    }
}
