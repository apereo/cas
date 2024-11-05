package org.apereo.cas.redis.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This is {@link RedisModulesOperations}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface RedisModulesOperations {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(RedisModulesOperations.class);

    /**
     * The default bean name.
     */
    String BEAN_NAME = "redisModulesOperations";

    /**
     * Create indexes.
     *
     * @param indexName the index name
     * @param prefix    the prefix
     * @param fields    the fields
     */
    default void createIndexes(final String indexName, final String prefix, final List<String> fields) {
    }

    /**
     * Search.
     *
     * @param searchIndexName the search index name
     * @param query           the query
     * @return the stream
     */
    default Stream<Map<String, String>> search(final String searchIndexName, final String query) {
        return Stream.empty();
    }
}
