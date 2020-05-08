package org.apereo.cas.couchbase.core;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;

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
import com.couchbase.client.java.query.QueryStatus;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
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
 */
@Slf4j
@Getter
public class CouchbaseClientFactory {
    private final BaseCouchbaseProperties properties;

    private Cluster cluster;

    /**
     * Instantiates a new Couchbase client factory.
     *
     * @param properties the properties
     */
    public CouchbaseClientFactory(final BaseCouchbaseProperties properties) {
        this.properties = properties;
        initializeCluster();
    }

    /**
     * Collect attributes from entity map.
     *
     * @param couchbaseEntity the couchbase entity
     * @param filter          the filter
     * @return the map
     */
    public static Map<String, List<Object>> collectAttributesFromEntity(final JsonObject couchbaseEntity,
                                                                        final Predicate<String> filter) {
        return couchbaseEntity.getNames()
            .stream()
            .filter(filter)
            .map(name -> Pair.of(name, couchbaseEntity.get(name)))
            .collect(Collectors.toMap(Pair::getKey, s -> CollectionUtils.wrapList(s.getValue())));
    }

    /**
     * Inverse of connectBucket, shuts down the client, cancelling connection
     * task if not completed.
     */
    @SneakyThrows
    public void shutdown() {
        if (this.cluster != null) {
            LOGGER.debug("Disconnecting from Couchbase cluster");
            this.cluster.disconnect();
        }
    }

    /**
     * Gets connection timeout.
     *
     * @return the connection timeout
     */
    public Duration getConnectionTimeout() {
        return Beans.newDuration(properties.getConnectionTimeout());
    }

    /**
     * Gets search timeout.
     *
     * @return the search timeout
     */
    public Duration getSearchTimeout() {
        return Beans.newDuration(properties.getSearchTimeout());
    }

    /**
     * Gets query timeout.
     *
     * @return the query timeout
     */
    public Duration getQueryTimeout() {
        return Beans.newDuration(properties.getQueryTimeout());
    }

    /**
     * Gets view timeout.
     *
     * @return the view timeout
     */
    public Duration getViewTimeout() {
        return Beans.newDuration(properties.getViewTimeout());
    }

    /**
     * Gets kv timeout.
     *
     * @return the kv timeout
     */
    public Duration getKvTimeout() {
        return Beans.newDuration(properties.getKvTimeout());
    }

    /**
     * Count long.
     *
     * @param query the query
     * @return the long
     */
    public long count(final String query) {
        return count(query, Optional.empty());
    }

    /**
     * Count long.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the long
     */
    public long count(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("SELECT count(*) as count FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        val result = executeQuery(options, formattedQuery);
        if (result.metaData().status() == QueryStatus.ERRORS) {
            throw new CouchbaseException("Could not execute query");
        }
        return result.rowsAsObject().get(0).getLong("count");
    }

    /**
     * Query with parameters.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the query result
     */
    public QueryResult select(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("SELECT * FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions().scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        return executeQuery(options, formattedQuery);
    }

    /**
     * Select query result.
     *
     * @param query   the query
     * @param options the options
     * @return the query result
     */
    public QueryResult select(final String query, final QueryOptions options) {
        return select(query, options, true);
    }

    /**
     * Select query result.
     *
     * @param query                  the query
     * @param options                the options
     * @param includeResultsInBucket the include results in bucket
     * @return the query result
     */
    public QueryResult select(final String query,
                              final QueryOptions options,
                              final boolean includeResultsInBucket) {
        val formattedQuery = String.format("SELECT %s* FROM `%s` WHERE %s",
            includeResultsInBucket ? StringUtils.EMPTY : properties.getBucket() + '.',
            properties.getBucket(), query);
        return executeQuery(options, formattedQuery);
    }

    /**
     * Query and get a result by username.
     *
     * @param statement the query
     * @return the n1ql query result
     */
    public QueryResult select(final String statement) {
        return select(statement, Optional.empty());
    }

    /**
     * Remove and return query result.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the query result
     */
    public QueryResult remove(final String query, final Optional<JsonObject> parameters) {
        val formattedQuery = String.format("DELETE FROM `%s` WHERE %s", properties.getBucket(), query);
        val options = QueryOptions.queryOptions()
            .scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()));
        parameters.ifPresent(options::parameters);
        return executeQuery(options, formattedQuery);
    }

    /**
     * Remove and return query result.
     *
     * @param query the query
     * @return the query result
     */
    public QueryResult remove(final String query) {
        return remove(query, Optional.empty());
    }

    /**
     * Remove all and return query result.
     *
     * @return the query result
     */
    public QueryResult removeAll() {
        return remove("1=1", Optional.empty());
    }

    /**
     * Bucket upsert default collection.
     *
     * @param content the content
     * @return the mutation result
     */
    public MutationResult bucketUpsertDefaultCollection(final String content) {
        val id = UUID.randomUUID().toString();
        val document = JsonObject.fromJson(content);
        return bucketUpsertDefaultCollection(id, document);
    }

    /**
     * Bucket upsert default collection.
     *
     * @param id       the id
     * @param document the document
     * @return the mutation result
     */
    public MutationResult bucketUpsertDefaultCollection(final String id, final Object document) {
        return bucketUpsertDefaultCollection(id, document, UpsertOptions.upsertOptions());
    }

    /**
     * Bucket upsert default collection mutation result.
     *
     * @param id       the id
     * @param document the document
     * @param options  the options
     * @return the mutation result
     */
    public MutationResult bucketUpsertDefaultCollection(final String id, final Object document,
                                                        final UpsertOptions options) {
        val bucket = this.cluster.bucket(properties.getBucket());
        return bucket.defaultCollection().upsert(id, document, options);
    }

    /**
     * Bucket remove from default collection.
     *
     * @param id the id
     * @return the mutation result
     */
    public Optional<MutationResult> bucketRemoveFromDefaultCollection(final String id) {
        val bucket = this.cluster.bucket(properties.getBucket());
        try {
            return Optional.of(bucket.defaultCollection().remove(id));
        } catch (final DocumentNotFoundException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Gets bucket.
     *
     * @return the bucket
     */
    public String getBucket() {
        return properties.getBucket();
    }

    /**
     * Bucket get get result.
     *
     * @param id the id
     * @return the get result
     */
    public GetResult bucketGet(final String id) {
        return bucketGet(id, GetOptions.getOptions());
    }

    public GetResult bucketGet(final String id, final GetOptions options) {
        val bucket = this.cluster.bucket(properties.getBucket());
        return bucket.defaultCollection().get(id, options);
    }

    private void initializeCluster() {
        shutdown();
        LOGGER.debug("Initializing Couchbase cluster for nodes [{}]", properties.getAddresses());

        val env = ClusterEnvironment
            .builder()
            .timeoutConfig(TimeoutConfig
                .connectTimeout(getConnectionTimeout())
                .kvTimeout(getKvTimeout())
                .queryTimeout(getQueryTimeout())
                .searchTimeout(getSearchTimeout())
                .viewTimeout(getViewTimeout()))
            .ioConfig(IoConfig
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

    private QueryResult executeQuery(final QueryOptions options,
                                     final String formattedQuery) {
        LOGGER.trace("Executing query [{}]", formattedQuery);
        options
            .scanConsistency(QueryScanConsistency.valueOf(properties.getScanConsistency()))
            .timeout(getConnectionTimeout())
            .scanWait(Beans.newDuration(properties.getScanWaitTimeout()));
        if (properties.getMaxParallelism() > 0) {
            options.maxParallelism(properties.getMaxParallelism());
        }
        val result = cluster.query(formattedQuery, options);
        if (result.metaData().status() == QueryStatus.ERRORS) {
            throw new CouchbaseException("Could not execute query");
        }
        return result;
    }
}

