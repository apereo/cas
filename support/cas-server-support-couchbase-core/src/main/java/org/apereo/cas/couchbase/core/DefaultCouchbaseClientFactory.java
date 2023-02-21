package org.apereo.cas.couchbase.core;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LoggingUtils;

import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.core.env.SeedNode;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.UpsertOptions;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A factory class which produces a client for a particular Couchbase getBucket.
 * A design consideration was that we want the server to start even if Couchbase
 * is unavailable, picking up the connection when Couchbase comes online. Hence
 * the creation of the client is made using a scheduled task which is repeated
 * until successful connection is made.
 *
 * @author Fredrik Jönsson "fjo@kth.se"
 * @author Misagh Moayyed
 * @since 4.2
 * @deprecated Since 7.0.0
 */
@Slf4j
@Getter
@Deprecated(since = "7.0.0")
public class DefaultCouchbaseClientFactory implements CouchbaseClientFactory {
    private final BaseCouchbaseProperties properties;

    private Cluster cluster;

    /**
     * Instantiates a new Couchbase client factory.
     *
     * @param properties the properties
     */
    public DefaultCouchbaseClientFactory(final BaseCouchbaseProperties properties) {
        this.properties = properties;
        initializeCluster();
    }

    /**
     * Inverse of connectBucket, shuts down the client, cancelling connection
     * task if not completed.
     */
    @Override
    public void shutdown() {
        if (this.cluster != null) {
            LOGGER.debug("Disconnecting from Couchbase cluster");
            this.cluster.disconnect();
        }
    }

    @Override
    public Duration getConnectionTimeout() {
        return Beans.newDuration(properties.getConnectionTimeout());
    }

    @Override
    public Duration getIdleConnectionTimeout() {
        return Beans.newDuration(properties.getIdleConnectionTimeout());
    }

    @Override
    public Duration getSearchTimeout() {
        return Beans.newDuration(properties.getSearchTimeout());
    }

    @Override
    public Duration getQueryTimeout() {
        return Beans.newDuration(properties.getQueryTimeout());
    }

    @Override
    public Duration getViewTimeout() {
        return Beans.newDuration(properties.getViewTimeout());
    }

    @Override
    public Duration getKvTimeout() {
        return Beans.newDuration(properties.getKvTimeout());
    }

    @Override
    public long count(final String query) {
        return count(query, Optional.empty());
    }

    @Override
    public long count(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("SELECT count(*) as count FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions()
            .readonly(true)
            .scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        val result = executeQuery(options, formattedQuery);
        return result.rowsAsObject().get(0).getLong("count");
    }

    @Override
    public QueryResult select(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("SELECT * FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        return executeQuery(options, formattedQuery);
    }

    @Override
    public QueryResult select(final String query, final QueryOptions options) {
        return select(query, options, true);
    }

    @Override
    public QueryResult select(final String query,
                              final QueryOptions options,
                              final boolean includeResultsInBucket) {
        val formattedQuery = String.format("SELECT %s* FROM `%s` WHERE %s",
            includeResultsInBucket ? StringUtils.EMPTY : properties.getBucket() + '.',
            properties.getBucket(), query);
        return executeQuery(options, formattedQuery);
    }

    @Override
    public QueryResult select(final String statement) {
        return select(statement, Optional.empty());
    }

    @Override
    public QueryResult remove(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("DELETE FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions()
            .scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        return executeQuery(options, formattedQuery);
    }

    @Override
    public QueryResult remove(final String query) {
        return remove(query, Optional.empty());
    }

    @Override
    public QueryResult removeAll() {
        return remove("1=1", Optional.empty());
    }

    @Override
    public MutationResult bucketUpsertDefaultCollection(final String content) {
        val id = UUID.randomUUID().toString();
        val document = JsonObject.fromJson(content);
        return bucketUpsertDefaultCollection(id, document);
    }

    @Override
    public MutationResult bucketUpsertDefaultCollection(final String id, final Object document) {
        return bucketUpsertDefaultCollection(id, document, UpsertOptions.upsertOptions());
    }

    @Override
    public MutationResult bucketUpsertDefaultCollection(final String id, final Object document,
                                                        final UpsertOptions options) {
        val bucket = this.cluster.bucket(properties.getBucket());
        return bucket.defaultCollection().upsert(id, document, options);
    }

    @Override
    public Optional<MutationResult> bucketRemoveFromDefaultCollection(final String id) {
        val bucket = this.cluster.bucket(properties.getBucket());
        try {
            return Optional.of(bucket.defaultCollection().remove(id));
        } catch (final DocumentNotFoundException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public String getBucket() {
        return properties.getBucket();
    }

    @Override
    public GetResult bucketGet(final String id) {
        return bucketGet(id, GetOptions.getOptions());
    }

    @Override
    public GetResult bucketGet(final String id, final GetOptions options) {
        try {
            val bucket = cluster.bucket(properties.getBucket());
            return bucket.defaultCollection().get(id, options);
        } catch (final DocumentNotFoundException e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }

    private void initializeCluster() {
        shutdown();
        LOGGER.debug("Initializing Couchbase cluster for nodes [{}]", properties.getAddresses());

        val env = ClusterEnvironment
            .builder()
            .maxNumRequestsInRetry(properties.getMaxNumRequestsInRetry())
            .timeoutConfig(TimeoutConfig
                .connectTimeout(getConnectionTimeout())
                .kvTimeout(getKvTimeout())
                .queryTimeout(getQueryTimeout())
                .searchTimeout(getSearchTimeout())
                .viewTimeout(getViewTimeout()))
            .ioConfig(IoConfig
                .idleHttpConnectionTimeout(getIdleConnectionTimeout())
                .maxHttpConnections(properties.getMaxHttpConnections())
                .networkResolution(NetworkResolution.AUTO))
            .build();

        val listOfNodes = properties.getAddresses()
            .stream()
            .map(SeedNode::create)
            .collect(Collectors.toSet());

        val options = ClusterOptions
            .clusterOptions(properties.getClusterUsername(), properties.getClusterPassword())
            .environment(env);
        this.cluster = Cluster.connect(listOfNodes, options);
    }

    @Override
    public QueryResult executeQuery(final QueryOptions options,
                                    final String formattedQuery) {
        LOGGER.trace("Executing query [{}]", formattedQuery);
        options
            .scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()))
            .timeout(getConnectionTimeout())
            .scanWait(Beans.newDuration(properties.getScanWaitTimeout()));
        if (properties.getMaxParallelism() > 0) {
            options.maxParallelism(properties.getMaxParallelism());
        }
        return cluster.query(formattedQuery, options);
    }
}

