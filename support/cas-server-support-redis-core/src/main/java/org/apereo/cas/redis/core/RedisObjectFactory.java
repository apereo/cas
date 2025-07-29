package org.apereo.cas.redis.core;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.function.FunctionUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link RedisObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class RedisObjectFactory {
    /**
     * New redis template.
     *
     * @param <K>               the type parameter
     * @param <V>               the type parameter
     * @param connectionFactory the connection factory
     * @param valueSerializer   the value serializer
     * @return the cas redis template
     */
    public static <K, V> CasRedisTemplate<K, V> newRedisTemplate(
        final RedisConnectionFactory connectionFactory,
        final RedisSerializer<?> valueSerializer) {
        val template = new DefaultCasRedisTemplate<K, V>();

        val stringRedisSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        val compressedSerializer = new Lz4CompressionRedisSerializer(valueSerializer);
        template.setValueSerializer(compressedSerializer);
        template.setHashValueSerializer(compressedSerializer);

        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * New redis template.
     *
     * @param <K>               the type parameter
     * @param <V>               the type parameter
     * @param connectionFactory the connection factory
     * @return the redis template
     */
    public static <K, V> CasRedisTemplate<K, V> newRedisTemplate(final RedisConnectionFactory connectionFactory) {
        val valueSerializer = new JdkSerializationRedisSerializer();
        return newRedisTemplate(connectionFactory, valueSerializer);
    }

    /**
     * New redis connection factory.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the redis connection factory
     */
    public static RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis,
                                                                   final CasSSLContext casSslContext) throws Exception {
        return newRedisConnectionFactory(redis, false, casSslContext);
    }

    /**
     * New redis connection factory.
     *
     * @param redis         the redis
     * @param initialize    the initialize
     * @param casSslContext the cas ssl context
     * @return the redis connection factory
     */
    public static RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis,
                                                                   final boolean initialize,
                                                                   final CasSSLContext casSslContext) throws Exception {
        var factory = (LettuceConnectionFactory) null;
        if (redis.getSentinel() != null && StringUtils.hasText(redis.getSentinel().getMaster())) {
            factory = new LettuceConnectionFactory(getSentinelConfig(redis), getRedisPoolClientConfig(redis, casSslContext));
        } else if (redis.getCluster() != null && !redis.getCluster().getNodes().isEmpty()) {
            factory = new LettuceConnectionFactory(getClusterConfig(redis), getRedisPoolClientConfig(redis, casSslContext));
        } else {
            factory = new LettuceConnectionFactory(getStandaloneConfig(redis), getRedisPoolClientConfig(redis, casSslContext));
        }
        var connectionSharingEnabled = redis.getPool() == null || !redis.getPool().isEnabled();
        if (redis.getShareNativeConnections() != null) {
            connectionSharingEnabled = redis.getShareNativeConnections();
        }
        LOGGER.info("Redis native connection sharing is turned [{}]", BooleanUtils.toStringOnOff(connectionSharingEnabled));
        factory.setShareNativeConnection(connectionSharingEnabled);

        if (initialize) {
            factory.afterPropertiesSet();
        }
        return factory;
    }

    private boolean isRedisClusteringEnabled(final BaseRedisProperties redis) {
        return (redis.getSentinel() != null && StringUtils.hasText(redis.getSentinel().getMaster()))
            || (redis.getCluster() != null && !redis.getCluster().getNodes().isEmpty());
    }

    private static RedisClusterConfiguration getClusterConfig(final BaseRedisProperties redis) {
        val redisConfiguration = new RedisClusterConfiguration();
        val cluster = redis.getCluster();

        cluster.getNodes()
            .stream()
            .filter(nodeConfig -> StringUtils.hasText(nodeConfig.getHost())
                && nodeConfig.getPort() > 0
                && StringUtils.hasText(nodeConfig.getType()))
            .forEach(nodeConfig -> {
                LOGGER.trace("Building redis cluster node for [{}]", nodeConfig);

                val nodeBuilder = new RedisNode.RedisNodeBuilder()
                    .listeningAt(nodeConfig.getHost(), nodeConfig.getPort())
                    .promotedAs(RedisNode.NodeType.valueOf(nodeConfig.getType().toUpperCase(Locale.ENGLISH)));

                if (StringUtils.hasText(nodeConfig.getReplicaOf())) {
                    nodeBuilder.replicaOf(nodeConfig.getReplicaOf());
                }
                if (StringUtils.hasText(nodeConfig.getId())) {
                    nodeBuilder.withId(nodeConfig.getId());
                }
                if (StringUtils.hasText(nodeConfig.getName())) {
                    nodeBuilder.withName(nodeConfig.getName());
                }
                redisConfiguration.clusterNode(nodeBuilder.build());
            });
        if (StringUtils.hasText(cluster.getPassword())) {
            redisConfiguration.setPassword(cluster.getPassword());
            redisConfiguration.setUsername(cluster.getUsername());
        }
        if (cluster.getMaxRedirects() > 0) {
            redisConfiguration.setMaxRedirects(cluster.getMaxRedirects());
        }
        return redisConfiguration;
    }

    private static LettucePoolingClientConfiguration getRedisPoolClientConfig(final BaseRedisProperties redis,
                                                                              final CasSSLContext casSslContext) throws Exception {
        var poolingClientConfig = LettucePoolingClientConfiguration.builder();
        if (redis.isUseSsl()) {
            val sslPoolingConfig = poolingClientConfig.useSsl();
            FunctionUtils.doWhen(!redis.isVerifyPeer(), __ -> sslPoolingConfig.disablePeerVerification());
            FunctionUtils.doWhen(redis.isStartTls(), __ -> sslPoolingConfig.startTls());
            LOGGER.trace("Redis configuration: SSL connections are enabled");
        }
        if (redis.getReadFrom() != null) {
            poolingClientConfig.readFrom(ReadFrom.valueOf(redis.getReadFrom().name()));
            LOGGER.debug("Redis configuration: readFrom property is set to [{}]", redis.getReadFrom());
        }

        if (StringUtils.hasText(redis.getTimeout())) {
            val commandTimeout = Beans.newDuration(redis.getTimeout());
            val commandTimeoutMillis = commandTimeout.toMillis();
            if (commandTimeoutMillis > 0) {
                poolingClientConfig.commandTimeout(Duration.ofMillis(commandTimeoutMillis));
                LOGGER.trace("Redis configuration: commandTimeout is set to [{}]ms", commandTimeoutMillis);
            }
        }

        val clientOptions = newClientOptions(redis, casSslContext);
        poolingClientConfig.clientOptions(clientOptions);

        val pool = redis.getPool();
        if (pool != null && pool.isEnabled()) {
            val config = new GenericObjectPoolConfig();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            config.setMaxWait(Beans.newDuration(pool.getMaxWait()));
            config.setLifo(pool.isLifo());
            config.setFairness(pool.isFairness());
            config.setTestWhileIdle(pool.isTestWhileIdle());
            config.setTestOnBorrow(pool.isTestOnBorrow());
            config.setTestOnReturn(pool.isTestOnReturn());
            config.setTestOnCreate(pool.isTestOnCreate());
            if (pool.getMinEvictableIdleTimeMillis() > 0) {
                config.setMinEvictableIdleDuration(Duration.ofMillis(pool.getMinEvictableIdleTimeMillis()));
            }
            if (pool.getNumTestsPerEvictionRun() > 0) {
                config.setNumTestsPerEvictionRun(pool.getNumTestsPerEvictionRun());
            }
            if (pool.getSoftMinEvictableIdleTimeMillis() > 0) {
                config.setSoftMinEvictableIdleDuration(Duration.ofMillis(pool.getSoftMinEvictableIdleTimeMillis()));
            }
            poolingClientConfig.poolConfig(config);
            LOGGER.trace("Redis configuration: the pool is configured to [{}]", config);
        }
        return poolingClientConfig.build();
    }

    /**
     * New client options.
     *
     * @param redis         the redis
     * @param casSslContext the cas ssl context
     * @return the client options
     * @throws Exception the exception
     */
    public static ClientOptions newClientOptions(final BaseRedisProperties redis,
                                                 final CasSSLContext casSslContext) throws Exception {
        val clientOptionsBuilder = initializeClientOptionsBuilder(redis);
        if (StringUtils.hasText(redis.getConnectTimeout())) {
            val connectTimeout = Beans.newDuration(redis.getConnectTimeout());
            clientOptionsBuilder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        val sslOptionsBuilder = SslOptions.builder()
            .jdkSslProvider()
            .keyManager(casSslContext.getKeyManagerFactory())
            .trustManager(casSslContext.getTrustManagerFactory());

        if ((redis.isStartTls() || redis.isUseSsl()) && redis.getKeyCertificateChainFile() != null && redis.getKeyFile() != null) {
            LOGGER.debug("Loading SSL certificate chain [{}] and key file [{}]",
                redis.getKeyCertificateChainFile().getCanonicalPath(), redis.getKeyFile().getCanonicalPath());
            sslOptionsBuilder.keyManager(redis.getKeyCertificateChainFile(), redis.getKeyFile(),
                StringUtils.hasText(redis.getKeyPassword()) ? redis.getKeyPassword().toCharArray() : null);
        }
        if (redis.isUseSsl() && redis.getCertificateFile() != null) {
            sslOptionsBuilder.trustManager(redis.getCertificateFile());
        }
        return clientOptionsBuilder
            .timeoutOptions(TimeoutOptions.enabled())
            .sslOptions(sslOptionsBuilder.build())
            .protocolVersion(ProtocolVersion.valueOf(redis.getProtocolVersion()))
            .build();

    }

    private static ClientOptions.Builder initializeClientOptionsBuilder(final BaseRedisProperties redis) {
        val cluster = isRedisClusteringEnabled(redis);
        if (cluster) {
            val refreshBuilder = ClusterTopologyRefreshOptions.builder()
                .dynamicRefreshSources(redis.getCluster().isDynamicRefreshSources());
            if (StringUtils.hasText(redis.getCluster().getTopologyRefreshPeriod())) {
                refreshBuilder.enablePeriodicRefresh(Beans.newDuration(redis.getCluster().getTopologyRefreshPeriod()));
            }
            if (redis.getCluster().isAdaptiveTopologyRefresh()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            val clusterClientOptionsBuilder = ClusterClientOptions.builder();
            return clusterClientOptionsBuilder.topologyRefreshOptions(refreshBuilder.build());
        }
        return ClientOptions.builder();
    }

    private static RedisConfiguration getStandaloneConfig(final BaseRedisProperties redis) {
        if (StringUtils.hasText(redis.getUri())) {
            LOGGER.debug("Setting Redis standalone configuration based on URI");
            return LettuceConnectionFactory.createRedisConfiguration(redis.getUri());
        }
        LOGGER.debug("Setting Redis standalone configuration on host [{}] and port [{}]", redis.getHost(), redis.getPort());
        val standaloneConfig = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standaloneConfig.setDatabase(redis.getDatabase());
        if (StringUtils.hasText(redis.getPassword())) {
            standaloneConfig.setUsername(redis.getUsername());
            standaloneConfig.setPassword(RedisPassword.of(redis.getPassword()));
        }
        return standaloneConfig;
    }

    private static RedisSentinelConfiguration getSentinelConfig(final BaseRedisProperties redis) {
        LOGGER.debug("Setting Redis with Sentinel configuration on master [{}]", redis.getSentinel().getMaster());
        val sentinelConfig = new RedisSentinelConfiguration()
            .master(redis.getSentinel().getMaster());
        LOGGER.debug("Sentinel nodes configured are [{}]", redis.getSentinel().getNode());
        sentinelConfig.setSentinels(createRedisNodesForProperties(redis));
        sentinelConfig.setDatabase(redis.getDatabase());
        sentinelConfig.setUsername(redis.getUsername());
        if (StringUtils.hasText(redis.getPassword())) {
            sentinelConfig.setPassword(RedisPassword.of(redis.getPassword()));
        }
        if (StringUtils.hasText(redis.getSentinel().getPassword())) {
            sentinelConfig.setSentinelPassword(RedisPassword.of(redis.getSentinel().getPassword()));
        }
        return sentinelConfig;
    }

    private static List<RedisNode> createRedisNodesForProperties(final BaseRedisProperties redis) {
        if (redis.getSentinel().getNode() != null) {
            val nodes = redis.getSentinel().getNode();
            return nodes
                .stream()
                .map(hostAndPort -> StringUtils.split(hostAndPort, ":"))
                .filter(Objects::nonNull)
                .map(args -> new RedisNode(args[0], Integer.parseInt(args[1])))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
