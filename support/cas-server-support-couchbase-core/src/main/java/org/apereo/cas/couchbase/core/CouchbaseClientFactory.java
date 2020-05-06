package org.apereo.cas.couchbase.core;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.error.DesignDocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    static {
        System.setProperty("com.couchbase.queryEnabled", "true");
    }

    private final Collection<View> views;

    /* Design document and views to create in the bucket, if any. */
    private final String designDocument;

    private final BaseCouchbaseProperties properties;

    private Cluster cluster;

    private Bucket bucket;

    public CouchbaseClientFactory(final BaseCouchbaseProperties properties,
                                  final String documentName, final Collection<View> views) {
        this.properties = properties;
        this.designDocument = documentName;
        this.views = views;
        initializeCluster();
    }

    public CouchbaseClientFactory(final BaseCouchbaseProperties properties) {
        this(properties, null, null);
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

    private void initializeCluster() {
        shutdown();
        val nodes = org.springframework.util.StringUtils.commaDelimitedListToSet(properties.getNodeSet());
        LOGGER.debug("Initializing Couchbase cluster for nodes [{}]", nodes);
        val listOfNodes = new ArrayList<>(nodes);
        var env = DefaultCouchbaseEnvironment.builder()
            .connectTimeout(getConnectionTimeout())
            .socketConnectTimeout(getSocketTimeout())
            .queryTimeout(getQueryTimeout())
            .searchTimeout(getSearchTimeout())
            .build();

        this.cluster = CouchbaseCluster.create(env, listOfNodes);
    }

    /**
     * Retrieve the Couchbase getBucket.
     *
     * @return the getBucket.
     */
    public Bucket getBucket() {
        if (this.bucket != null) {
            return this.bucket;
        }
        initializeBucket();
        return this.bucket;
    }

    public long getConnectionTimeout() {
        return Beans.newDuration(properties.getConnectionTimeout()).toMillis();
    }

    public long getSearchTimeout() {
        return Beans.newDuration(properties.getSearchTimeout()).toMillis();
    }

    public long getQueryTimeout() {
        return Beans.newDuration(properties.getQueryTimeout()).toMillis();
    }

    public int getSocketTimeout() {
        return (int) Beans.newDuration(properties.getSocketTimeout()).toMillis();
    }

    /**
     * Query and get a result by username.
     *
     * @param usernameAttribute the username attribute
     * @param usernameValue     the username value
     * @return the n1ql query result
     * @throws GeneralSecurityException the general security exception
     */
    public N1qlQueryResult query(final String usernameAttribute, final String usernameValue) throws GeneralSecurityException {
        val theBucket = getBucket();
        val statement = Select.select("*")
            .from(Expression.i(theBucket.name()))
            .where(Expression.x(usernameAttribute).eq('\'' + usernameValue + '\''));

        LOGGER.debug("Running query [{}] on bucket [{}]", statement.toString(), theBucket.name());

        val query = N1qlQuery.simple(statement);
        val result = theBucket.query(query, getConnectionTimeout(), TimeUnit.MILLISECONDS);
        if (!result.finalSuccess()) {
            LOGGER.error("Couchbase query failed with [{}]", result.errors()
                .stream()
                .map(JsonObject::toString)
                .collect(Collectors.joining(",")));
            throw new GeneralSecurityException("Could not locate account for user " + usernameValue);
        }
        return result;
    }

    /**
     * Collect attributes from entity map.
     *
     * @param couchbaseEntity the couchbase entity
     * @param filter          the filter
     * @return the map
     */
    public Map<String, List<Object>> collectAttributesFromEntity(final JsonObject couchbaseEntity, final Predicate<String> filter) {
        return couchbaseEntity.getNames()
            .stream()
            .filter(filter)
            .map(name -> Pair.of(name, couchbaseEntity.get(name)))
            .collect(Collectors.toMap(Pair::getKey, s -> CollectionUtils.wrapList(s.getValue())));
    }

    private void initializeBucket() {
        openBucket();
        createDesignDocumentAndViewIfNeeded();
    }

    private void createDesignDocumentAndViewIfNeeded() {
        if (this.views != null && this.designDocument != null) {
            LOGGER.debug("Ensure that indexes exist in bucket [{}]", this.bucket.name());
            val bucketManager = this.bucket.bucketManager();
            val newDocument = DesignDocument.create(this.designDocument, new ArrayList<>(views));
            try {
                if (!newDocument.equals(bucketManager.getDesignDocument(this.designDocument))) {
                    LOGGER.warn("Missing indexes in bucket [{}] for document [{}]", this.bucket.name(), this.designDocument);
                    bucketManager.upsertDesignDocument(newDocument);
                }
            } catch (final DesignDocumentDoesNotExistException e) {
                LOGGER.debug("Design document in bucket [{}] for document [{}] should be created", this.bucket.name(), this.designDocument);
                bucketManager.upsertDesignDocument(newDocument);
            } catch (final Exception e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

    private void openBucket() {
        try {
            LOGGER.trace("Trying to connect to couchbase bucket [{}]", properties.getBucket());
            if (StringUtils.isBlank(properties.getPassword())) {
                this.bucket = this.cluster.openBucket(properties.getBucket(),
                    getConnectionTimeout(), TimeUnit.MILLISECONDS);
            } else {
                this.bucket = this.cluster.openBucket(properties.getBucket(), properties.getPassword(),
                    getConnectionTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to connect to Couchbase bucket " + properties.getBucket(), e);
        }
        LOGGER.info("Connected to Couchbase bucket [{}]", properties.getBucket());
    }
}

