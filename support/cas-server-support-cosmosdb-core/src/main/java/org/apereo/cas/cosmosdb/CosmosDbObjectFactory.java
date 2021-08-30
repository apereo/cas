package org.apereo.cas.cosmosdb;

import org.apereo.cas.configuration.model.support.cosmosdb.BaseCosmosDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ThroughputProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpStatus;

/**
 * This is {@link CosmosDbObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class CosmosDbObjectFactory {
    private final BaseCosmosDbProperties properties;

    /**
     * Build client.
     *
     * @return the cosmos client
     */
    public CosmosClient buildClient() {
        val throttlingRetryOptions = new ThrottlingRetryOptions()
            .setMaxRetryAttemptsOnThrottledRequests(properties.getMaxRetryAttemptsOnThrottledRequests())
            .setMaxRetryWaitTime(Beans.newDuration(properties.getMaxRetryWaitTime()));

        return new CosmosClientBuilder()
            .endpoint(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUri()))
            .key(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getKey()))
            .preferredRegions(this.properties.getPreferredRegions())
            .consistencyLevel(ConsistencyLevel.valueOf(properties.getConsistencyLevel()))
            .contentResponseOnWriteEnabled(false)
            .clientTelemetryEnabled(properties.isAllowTelemetry())
            .userAgentSuffix(properties.getUserAgentSuffix())
            .throttlingRetryOptions(throttlingRetryOptions)
            .endpointDiscoveryEnabled(properties.isEndpointDiscoveryEnabled())
            .directMode()
            .buildClient();
    }

    /**
     * Gets container.
     *
     * @param name the name
     * @return the container
     */
    public CosmosContainer getContainer(final String name) {
        val client = buildClient();
        val databaseResponse = client.createDatabaseIfNotExists(properties.getDatabase());
        val database = client.getDatabase(databaseResponse.getProperties().getId());
        return database.getContainer(name);
    }

    /**
     * Drop container.
     *
     * @param name the name
     */
    public void deleteContainer(final String name) {
        val client = buildClient();
        val database = client.getDatabase(properties.getDatabase());
        val container = database.getContainer(name);
        if (container != null) {
            try {
                container.delete();
            } catch (final CosmosException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                    LoggingUtils.warn(LOGGER, e);
                }
            }
        }
    }

    /**
     * Create database.
     */
    public void createDatabaseIfNecessary() {
        val client = buildClient();
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
        val client = buildClient();
        val database = client.getDatabase(properties.getDatabase());
        val containerProperties = new CosmosContainerProperties(name, '/' + partitionKey);
        containerProperties.setIndexingPolicy(new IndexingPolicy()
            .setIndexingMode(IndexingMode.valueOf(properties.getIndexingMode())));
        val response = database.createContainerIfNotExists(containerProperties);
        LOGGER.debug(response.getProperties().getId());
    }
}
