package org.apereo.cas.redis.modules;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisURI;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    /**
     * New redis modules commands.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the optional
     * @throws Exception the exception
     */
    static RedisModulesCommands newRedisModulesCommands(
        final BaseRedisProperties redis, final CasSSLContext casSslContext) throws Exception {
        val uriBuilder = RedisURI.builder()
            .withStartTls(redis.isStartTls())
            .withVerifyPeer(redis.isVerifyPeer())
            .withHost(redis.getHost())
            .withPort(redis.getPort())
            .withDatabase(redis.getDatabase())
            .withSsl(redis.isUseSsl());

        if (StringUtils.hasText(redis.getUsername()) && StringUtils.hasText(redis.getPassword())) {
            uriBuilder.withAuthentication(redis.getUsername(), redis.getPassword());
        } else if (StringUtils.hasText(redis.getPassword())) {
            uriBuilder.withPassword(redis.getPassword().toCharArray());
        }

        val redisModulesClient = RedisModulesClient.create(uriBuilder.build());
        val clientOptions = RedisObjectFactory.newClientOptions(redis, casSslContext);
        redisModulesClient.setOptions(clientOptions);
        val connection = redisModulesClient.connect();
        val result = connection.sync();
        try {
            result.ftInfo(UUID.randomUUID().toString());
        } catch (final RedisCommandExecutionException e) {
            if (e.getMessage().contains("ERR unknown command")) {
                LOGGER.trace(e.getMessage(), e);
                throw new UnsupportedOperationException("Redis server does not support Redis Modules");
            }
        }
        return result;
    }
}
