package org.apereo.cas.redis.modules;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisModulesOperations;
import org.apereo.cas.redis.core.RedisObjectFactory;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.search.SearchReply;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This is {@link LettuceRedisModulesOperations}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class LettuceRedisModulesOperations implements RedisModulesOperations {
    private final RedisModulesCommands commands;

    @Override
    public void createIndexes(final String indexName, final String prefix,
                              final List<String> fields) {
        val options = CreateOptions.builder()
            .prefix(prefix)
            .maxTextFields(true)
            .build();
        val createIndex = commands.ftList().parallelStream().noneMatch(idx -> indexName.equalsIgnoreCase(idx.toString()));
        if (createIndex) {
            val indexFields = fields.stream().map(field -> Field.text(field).build()).toList();
            commands.ftCreate(indexName, options, indexFields.toArray(new Field[]{}));
        }
    }

    @Override
    public Stream<Map<String, String>> search(final String searchIndexName, final String query) {
        val results = (List<SearchReply.SearchResult>) commands.ftSearch(searchIndexName, query).getResults();
        return results.parallelStream().map(SearchReply.SearchResult::getFields);

    }

    /**
     * New redis modules commands.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the optional
     * @throws Exception the exception
     */
    public static RedisModulesCommands newRedisModulesCommands(
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
