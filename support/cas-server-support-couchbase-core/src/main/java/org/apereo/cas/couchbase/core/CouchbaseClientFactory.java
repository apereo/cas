package org.apereo.cas.couchbase.core;

import org.apereo.cas.util.CollectionUtils;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.UpsertOptions;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public interface CouchbaseClientFactory {

    /**
     * Collect attributes from entity map.
     *
     * @param couchbaseEntity the couchbase entity
     * @param filter          the filter
     * @return the map
     */
    static Map<String, List<Object>> collectAttributesFromEntity(final JsonObject couchbaseEntity,
                                                                 final Predicate<String> filter) {
        return couchbaseEntity.getNames()
            .stream()
            .filter(filter)
            .map(name -> Pair.of(name, couchbaseEntity.get(name)))
            .collect(Collectors.toMap(Pair::getKey, s -> CollectionUtils.wrapList(s.getValue())));
    }

    /**
     * Shutdown.
     */
    void shutdown();

    /**
     * Gets connection timeout.
     *
     * @return the connection timeout
     */
    Duration getConnectionTimeout();

    /**
     * Gets idle connection timeout.
     *
     * @return the idle connection timeout
     */
    Duration getIdleConnectionTimeout();

    /**
     * Gets search timeout.
     *
     * @return the search timeout
     */
    Duration getSearchTimeout();

    /**
     * Gets query timeout.
     *
     * @return the query timeout
     */
    Duration getQueryTimeout();

    /**
     * Gets view timeout.
     *
     * @return the view timeout
     */
    Duration getViewTimeout();

    /**
     * Gets kv timeout.
     *
     * @return the kv timeout
     */
    Duration getKvTimeout();

    /**
     * Count long.
     *
     * @param query the query
     * @return the long
     */
    long count(String query);

    /**
     * Count long.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the long
     */
    long count(String query, Optional<JsonObject> parameters);

    /**
     * Select query result.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the query result
     */
    QueryResult select(String query, Optional<JsonObject> parameters);

    /**
     * Select query result.
     *
     * @param query   the query
     * @param options the options
     * @return the query result
     */
    QueryResult select(String query, QueryOptions options);

    /**
     * Select query result.
     *
     * @param query                  the query
     * @param options                the options
     * @param includeResultsInBucket the include results in bucket
     * @return the query result
     */
    QueryResult select(String query,
                       QueryOptions options,
                       boolean includeResultsInBucket);

    /**
     * Select query result.
     *
     * @param statement the statement
     * @return the query result
     */
    QueryResult select(String statement);

    /**
     * Remove query result.
     *
     * @param query      the query
     * @param parameters the parameters
     * @return the query result
     */
    QueryResult remove(String query, Optional<JsonObject> parameters);

    /**
     * Remove query result.
     *
     * @param query the query
     * @return the query result
     */
    QueryResult remove(String query);

    /**
     * Remove all query result.
     *
     * @return the query result
     */
    QueryResult removeAll();

    /**
     * Bucket upsert default collection mutation result.
     *
     * @param content the content
     * @return the mutation result
     */
    MutationResult bucketUpsertDefaultCollection(String content);

    /**
     * Bucket upsert default collection mutation result.
     *
     * @param id       the id
     * @param document the document
     * @return the mutation result
     */
    MutationResult bucketUpsertDefaultCollection(String id, Object document);

    /**
     * Bucket upsert default collection mutation result.
     *
     * @param id       the id
     * @param document the document
     * @param options  the options
     * @return the mutation result
     */
    MutationResult bucketUpsertDefaultCollection(String id, Object document,
                                                 UpsertOptions options);

    /**
     * Bucket remove from default collection optional.
     *
     * @param id the id
     * @return the optional
     */
    Optional<MutationResult> bucketRemoveFromDefaultCollection(String id);

    /**
     * Gets bucket.
     *
     * @return the bucket
     */
    String getBucket();

    /**
     * Bucket get get result.
     *
     * @param id the id
     * @return the get result
     */
    GetResult bucketGet(String id);

    /**
     * Bucket get get result.
     *
     * @param id      the id
     * @param options the options
     * @return the get result
     */
    GetResult bucketGet(String id, GetOptions options);

    /**
     * Execute query query result.
     *
     * @param options        the options
     * @param formattedQuery the formatted query
     * @return the query result
     */
    QueryResult executeQuery(QueryOptions options,
                             String formattedQuery);

    /**
     * Gets cluster.
     *
     * @return the cluster
     */
    Cluster getCluster();
}
