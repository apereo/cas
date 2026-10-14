package org.apereo.cas.redis.modules;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.redis.core.RedisModulesOperations;
import org.apereo.cas.redis.core.RedisObjectFactory;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RediSearchCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.search.FieldValue;
import io.lettuce.core.search.SearchReply;
import io.lettuce.core.search.arguments.CreateArgs;
import io.lettuce.core.search.arguments.FieldArgs;
import io.lettuce.core.search.arguments.SearchArgs;
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
    private static final long SEARCH_RESULTS_PAGE_SIZE = 1000;

    private final RediSearchCommands<String> rediSearchCommands;

    @Override
    public void createIndexes(final String indexName, final String prefix,
                              final List<String> fields) {

        val options = CreateArgs.builder()
            .withPrefix(prefix)
            .maxTextFields()
            .build();
        val createIndex = rediSearchCommands.ftList().parallelStream().noneMatch(indexName::equalsIgnoreCase);
        if (createIndex) {
            val indexFields = fields.stream()
                .<FieldArgs>map(field -> TextFieldArgs.builder().name(field).build())
                .toList();
            try {
                rediSearchCommands.ftCreate(indexName, options, indexFields);
            } catch (final RedisCommandExecutionException e) {
                if (e.getMessage() == null || !e.getMessage().contains("Index already exists")) {
                    throw e;
                }
                LOGGER.debug("Search index [{}] was created concurrently by another client", indexName);
            }
        }
    }

    @Override
    public Stream<Map<String, String>> search(final String searchIndexName, final String query) {
        var reply = rediSearchCommands.ftSearch(searchIndexName, query, limitResultsTo(SEARCH_RESULTS_PAGE_SIZE));
        if (reply.getCount() > reply.getResults().size()) {
            reply = rediSearchCommands.ftSearch(searchIndexName, query, limitResultsTo(reply.getCount()));
        }
        return reply.getResults().parallelStream().map(LettuceRedisModulesOperations::toFieldValues);
    }

    private static SearchArgs<String> limitResultsTo(final long count) {
        return SearchArgs.<String>builder().limit(0, count).build();
    }

    /**
     * New redi search commands.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the optional
     * @throws Exception the exception
     */
    public static RediSearchCommands<String> newRediSearchCommands(
        final BaseRedisProperties redis, final CasSSLContext casSslContext)
            throws Exception {

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

        val redisClient = RedisClient.create(uriBuilder.build());
        val clientOptions = RedisObjectFactory.newClientOptions(redis, casSslContext);
        redisClient.setOptions(clientOptions);
        val connection = redisClient.connect();
        val commands = connection.sync();
        verifyRedisSearchSupport(commands);
        return commands;
    }

    private static Map<String, String> toFieldValues(final SearchReply.SearchResult<String> result) {
        val fields = new LinkedHashMap<String, String>();
        result.getFields().forEach((name, value) -> {
            if (value != null && !value.isNull()) {
                fields.put(name, value.getKind() == FieldValue.Kind.SCALAR ? value.asString() : value.toString());
            }
        });
        return fields;
    }

    private static void verifyRedisSearchSupport(final RediSearchCommands<String> commands) {
        try {
            commands.ftList();
        } catch (final RedisCommandExecutionException e) {
            LOGGER.trace(e.getMessage(), e);
            if (e.getMessage().contains("ERR unknown command")) {
                throw new UnsupportedOperationException("Redis server does not support Redis Search");
            }
        }
    }

    private static RediSearchCommands<String> newClusterRediSearchCommands(
        final BaseRedisProperties redis, final CasSSLContext casSslContext)
            throws Exception {
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
        val redisClusterClient = RedisClusterClient.create(redisUris);
        val clientOptions = (ClusterClientOptions) RedisObjectFactory.newClientOptions(redis, casSslContext);
        redisClusterClient.setOptions(clientOptions);
        val connection = redisClusterClient.connect();
        val commands = connection.sync();
        verifyRedisSearchSupport(commands);
        return commands;
    }

}
