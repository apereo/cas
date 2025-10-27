package org.apereo.cas.cosmosdb;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.cosmosdb.BaseCosmosDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.ThroughputProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.data.util.ReflectionUtils;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link CosmosDbObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 8.0.0
 */
@Slf4j
@Deprecated(since = "8.0.0", forRemoval = true)
public class CosmosDbObjectFactory {
    private final BaseCosmosDbProperties properties;

    private final CosmosClient client;

    public CosmosDbObjectFactory(final BaseCosmosDbProperties properties,
                                 final CasSSLContext casSSLContext) throws Exception {
        this.properties = properties;
        val throttlingRetryOptions = new ThrottlingRetryOptions()
            .setMaxRetryAttemptsOnThrottledRequests(properties.getMaxRetryAttemptsOnThrottledRequests())
            .setMaxRetryWaitTime(Beans.newDuration(properties.getMaxRetryWaitTime()));

        val uri = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUri());
        var builder = new CosmosClientBuilder()
            .endpoint(uri)
            .key(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getKey()))
            .preferredRegions(properties.getPreferredRegions())
            .consistencyLevel(ConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .contentResponseOnWriteEnabled(false)
            .userAgentSuffix(properties.getUserAgentSuffix())
            .throttlingRetryOptions(throttlingRetryOptions)
            .endpointDiscoveryEnabled(properties.isEndpointDiscoveryEnabled());

        if (Strings.CI.equals(properties.getMode(), "gateway")) {
            builder = builder.gatewayMode();
        } else if (Strings.CI.equals(properties.getMode(), "direct")) {
            builder = builder.directMode();
        }
        
        LOGGER.debug("Building CosmosDb client for [{}]", uri);
        val sslContext = SslContextBuilder
            .forClient()
            .sslProvider(SslProvider.JDK)
            .trustManager(casSSLContext.getTrustManagerFactory())
            .keyManager(casSSLContext.getKeyManagerFactory())
            .build();
        val cosmosDbConfigs = new CosmosDbConfigs(sslContext);
        val configs = ReflectionUtils.getRequiredField(builder.getClass(), "configs");
        configs.trySetAccessible();
        configs.set(builder, cosmosDbConfigs);
        this.client = builder.buildClient();
    }

    /**
     * Gets container.
     *
     * @param name the name
     * @return the container
     */
    public CosmosContainer getContainer(final String name) {
        LOGGER.debug("Fetching CosmosDb database [{}]", properties.getDatabase());
        val databaseResponse = client.createDatabaseIfNotExists(properties.getDatabase());
        val database = client.getDatabase(databaseResponse.getProperties().getId());
        LOGGER.debug("Fetching CosmosDb container [{}]", name);
        return database.getContainer(name);
    }

    /**
     * Create database.
     */
    public void createDatabaseIfNecessary() {
        val response = client.createDatabaseIfNotExists(properties.getDatabase(),
            ThroughputProperties.createAutoscaledThroughput(properties.getDatabaseThroughput()));
        LOGGER.debug("Created/Located database [{}]", response.getProperties().getId());
    }

    /**
     * Drop database.
     */
    public void dropDatabase() {
        client.getDatabase(properties.getDatabase()).delete();
        LOGGER.debug("Removed database [{}]", properties.getDatabase());
    }

    /**
     * Create container.
     *
     * @param name         the name
     * @param timeout      the timeout
     * @param partitionKey the partition key
     * @return the cosmos container
     */
    public CosmosContainer createContainer(final String name, final Long timeout, final String... partitionKey) {
        val database = client.getDatabase(properties.getDatabase());
        LOGGER.debug("Creating CosmosDb container [{}]", name);

        val partitionDefn = new PartitionKeyDefinition();
        partitionDefn.setPaths(Arrays.stream(partitionKey).map(key -> '/' + key).collect(Collectors.toList()));
        partitionDefn.setKind(PartitionKind.HASH);
        val containerProperties = new CosmosContainerProperties(name, partitionDefn);
        containerProperties.setIndexingPolicy(new IndexingPolicy()
            .setIndexingMode(IndexingMode.valueOf(properties.getIndexingMode())));
        containerProperties.setDefaultTimeToLiveInSeconds(timeout.intValue());
        val response = database.createContainerIfNotExists(containerProperties);
        LOGGER.debug("Created CosmosDb container [{}]", response.getProperties().getId());
        return getContainer(name);
    }

    /**
     * Create container.
     *
     * @param name         the name
     * @param partitionKey the partition key
     */
    public CosmosContainer createContainer(final String name, final String... partitionKey) {
        return createContainer(name, -1L, partitionKey);
    }

    @RequiredArgsConstructor
    private static final class CosmosDbConfigs extends Configs {
        private final SslContext sslContext;

        @Override
        public SslContext getSslContext(final boolean serverCertValidationDisabled, final boolean http2Enabled) {
            return sslContext;
        }
    }
}
