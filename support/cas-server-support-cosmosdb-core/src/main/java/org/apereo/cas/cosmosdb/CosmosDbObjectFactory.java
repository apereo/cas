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
import com.azure.cosmos.models.ThroughputProperties;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.util.ReflectionUtils;

/**
 * This is {@link CosmosDbObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CosmosDbObjectFactory {
    private final BaseCosmosDbProperties properties;

    private final CosmosClient client;

    @SneakyThrows
    public CosmosDbObjectFactory(final BaseCosmosDbProperties properties,
                                 final CasSSLContext casSSLContext) {
        this.properties = properties;
        val throttlingRetryOptions = new ThrottlingRetryOptions()
            .setMaxRetryAttemptsOnThrottledRequests(properties.getMaxRetryAttemptsOnThrottledRequests())
            .setMaxRetryWaitTime(Beans.newDuration(properties.getMaxRetryWaitTime()));

        val uri = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUri());
        val builder = new CosmosClientBuilder()
            .endpoint(uri)
            .key(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getKey()))
            .preferredRegions(this.properties.getPreferredRegions())
            .consistencyLevel(ConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .contentResponseOnWriteEnabled(false)
            .clientTelemetryEnabled(properties.isAllowTelemetry())
            .userAgentSuffix(properties.getUserAgentSuffix())
            .throttlingRetryOptions(throttlingRetryOptions)
            .endpointDiscoveryEnabled(properties.isEndpointDiscoveryEnabled())
            .directMode();
        LOGGER.debug("Building CosmosDb client for [{}]", uri);
        val sslContext = SslContextBuilder
            .forClient()
            .sslProvider(SslProvider.JDK)
            .trustManager(casSSLContext.getTrustManagerFactory())
            .build();
        val configsMethod = ReflectionUtils.findRequiredMethod(builder.getClass(), "configs");
        configsMethod.trySetAccessible();
        val configs = (Configs) configsMethod.invoke(builder);
        val sslContextField = ReflectionUtils.findRequiredField(configs.getClass(), "sslContext");
        sslContextField.trySetAccessible();
        sslContextField.set(configs, sslContext);
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
     * Create container.
     *
     * @param name         the name
     * @param partitionKey the partition key
     */
    public void createContainer(final String name, final String partitionKey) {
        val database = client.getDatabase(properties.getDatabase());
        LOGGER.debug("Creating CosmosDb container [{}]", name);
        val containerProperties = new CosmosContainerProperties(name, '/' + partitionKey);
        containerProperties.setIndexingPolicy(new IndexingPolicy()
            .setIndexingMode(IndexingMode.valueOf(properties.getIndexingMode())));
        val response = database.createContainerIfNotExists(containerProperties);
        LOGGER.debug("Created CosmosDb container [{}]", response.getProperties().getId());
    }
}
