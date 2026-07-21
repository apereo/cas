package org.apereo.cas.redis.modules;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisModulesOperations;
import org.apereo.cas.redis.core.RedisObjectFactory;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.cluster.RedisModulesClusterClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.search.SearchReply;
import io.lettuce.core.search.arguments.CreateArgs;
import io.lettuce.core.search.arguments.TextFieldArgs;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;

/**
 * This is {@link LettuceRedisModulesOperations}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class LettuceRedisModulesOperations implements RedisModulesOperations {
    private final RediSearchCommands rediSearchCommands;

    @Override
    public void createIndexes(final String indexName, final String prefix,
                              final List<String> fields) {

        val options = CreateArgs.builder()
            .withPrefix(prefix)
            .maxTextFields()
            .build();
        val createIndex = rediSearchCommands.ftList().parallelStream().noneMatch(idx -> indexName.equalsIgnoreCase(idx.toString()));
        if (createIndex) {
            val indexFields = fields.stream().map(field -> TextFieldArgs.builder().name(field).build()).toList();
            rediSearchCommands.ftCreate(indexName, options, indexFields);
        }
    }

    @Override
    public Stream<Map<String, String>> search(final String searchIndexName, final String query) {
        val results = (List<SearchReply.SearchResult>) rediSearchCommands.ftSearch(searchIndexName, query).getResults();
        return results.parallelStream().map(SearchReply.SearchResult::getFields);

    }

    /**
     * New redi search commands.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the optional
     * @throws Exception the exception
     */
    public static RediSearchCommands newRediSearchCommands(
        final BaseRedisProperties redis, final CasSSLContext casSslContext) throws Exception {

        if (redis.getCluster() != null && !redis.getCluster().getNodes().isEmpty()) {
            return newClusterRediSearchCommands(redis, casSslContext);
        }

        var uriBuilder = RedisURI.builder()
            .withStartTls(redis.isStartTls())
            .withVerifyPeer(redis.isVerifyPeer())
            .withDatabase(redis.getDatabase())
            .withSsl(redis.isUseSsl());

        if (StringUtils.hasText(redis.getUsername()) && StringUtils.hasText(redis.getPassword())) {
            uriBuilder = uriBuilder.withAuthentication(redis.getUsername(), redis.getPassword());
        } else if (StringUtils.hasText(redis.getPassword())) {
            uriBuilder = uriBuilder.withPassword(redis.getPassword().toCharArray());
        }
        if (redis.getSentinel() != null && StringUtils.hasText(redis.getSentinel().getMaster())) {
            uriBuilder = uriBuilder.withSentinelMasterId(redis.getSentinel().getMaster());
            val nodes = redis.getSentinel().getNode();
            for (val node : nodes) {
                val hostAndPort = Objects.requireNonNull(StringUtils.split(node, ":"));
                uriBuilder = uriBuilder.withSentinel(hostAndPort[0],
                    Integer.parseInt(hostAndPort[1]),
                    redis.getSentinel().getPassword());
            }
        } else {
            uriBuilder = uriBuilder.withHost(redis.getHost()).withPort(redis.getPort());
        }

        val redisModulesClient = RedisModulesClient.create(uriBuilder.build());
        val clientOptions = RedisObjectFactory.newClientOptions(redis, casSslContext);
        redisModulesClient.setOptions(clientOptions);
        val connection = redisModulesClient.connect();
        val commands = connection.sync();
        verifyRedisModulesSupport(commands);
        return commands;
    }

    private static void verifyRedisModulesSupport(final RedisModulesCommands<String, String> commands) {
        try {
            commands.ftInfo(UUID.randomUUID().toString());
        } catch (final RedisCommandExecutionException e) {
            LOGGER.trace(e.getMessage(), e);
            if (e.getMessage().contains("ERR unknown command")) {
                throw new UnsupportedOperationException("Redis server does not support Redis Modules");
            }
        }
    }

    private static RediSearchCommands newClusterRediSearchCommands(
        final BaseRedisProperties redis, final CasSSLContext casSslContext) throws Exception {
        val redisUris = redis.getCluster()
            .getNodes()
            .stream()
            .map(node -> {
                var builder = RedisURI.builder()
                    .withStartTls(redis.isStartTls())
                    .withVerifyPeer(redis.isVerifyPeer())
                    .withSsl(redis.isUseSsl());
                if (StringUtils.hasText(redis.getUsername()) && StringUtils.hasText(redis.getPassword())) {
                    builder = builder.withAuthentication(redis.getUsername(), redis.getPassword());
                } else if (StringUtils.hasText(redis.getPassword())) {
                    builder = builder.withPassword(redis.getPassword().toCharArray());
                }
                return builder
                    .withHost(node.getHost())
                    .withPort(node.getPort())
                    .build();
            })
            .toList();
        val redisModulesClient = RedisModulesClusterClient.create(redisUris);
        val clientOptions = (ClusterClientOptions) RedisObjectFactory.newClientOptions(redis, casSslContext);
        redisModulesClient.setOptions(clientOptions);
        val connection = redisModulesClient.connect();
        val commands = connection.sync();
        verifyRedisModulesSupport(commands);
        return commands;
    }

}
