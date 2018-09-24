package org.apereo.cas.couchbase.core;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DesignDocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
public class CouchbaseClientFactory {
    private static final long DEFAULT_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(15);

    static {
        System.setProperty("com.couchbase.queryEnabled", "true");
    }

    private final Collection<View> views;
    private final Set<String> nodes;
    /* Design document and views to create in the bucket, if any. */
    private final String designDocument;
    private Cluster cluster;
    private Bucket bucket;
    /* The name of the bucket, will use the default getBucket unless otherwise specified. */
    private String bucketName = "default";
    /* Password for the bucket if any. */
    private String bucketPassword = StringUtils.EMPTY;
    private long timeout = DEFAULT_TIMEOUT_MILLIS;

    /**
     * Instantiates a new Couchbase client factory.
     *
     * @param nodes          cluster nodes
     * @param bucketName     getBucket name
     * @param bucketPassword the bucket password
     * @param timeout        connection timeout
     * @param documentName   the document name
     * @param views          the views
     */
    public CouchbaseClientFactory(final Set<String> nodes, final String bucketName,
                                  final String bucketPassword, final long timeout,
                                  final String documentName, final Collection<View> views) {
        this.nodes = nodes;
        this.bucketName = bucketName;
        this.bucketPassword = bucketPassword;
        this.timeout = timeout;
        this.designDocument = documentName;
        this.views = views;
        initializeCluster();
    }

    /**
     * Instantiates a new Couchbase client factory.
     *
     * @param nodes          the nodes
     * @param bucketName     the bucket name
     * @param bucketPassword the bucket password
     */
    public CouchbaseClientFactory(final Set<String> nodes, final String bucketName, final String bucketPassword) {
        this(nodes, bucketName, bucketPassword, DEFAULT_TIMEOUT_MILLIS, null, null);
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
        if (this.cluster != null) {
            shutdown();
        }
        LOGGER.debug("Initializing Couchbase cluster for nodes [{}]", this.nodes);
        this.cluster = CouchbaseCluster.create(new ArrayList<>(this.nodes));
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
        final Statement statement = Select.select("*")
            .from(Expression.i(theBucket.name()))
            .where(Expression.x(usernameAttribute).eq('\'' + usernameValue + '\''));

        LOGGER.debug("Running query [{}] on bucket [{}]", statement.toString(), theBucket.name());

        val query = N1qlQuery.simple(statement);
        val result = theBucket.query(query, timeout, TimeUnit.MILLISECONDS);
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
    public Map<String, Object> collectAttributesFromEntity(final JsonObject couchbaseEntity, final Predicate<String> filter) {
        return couchbaseEntity.getNames()
            .stream()
            .filter(filter)
            .map(name -> Pair.of(name, couchbaseEntity.get(name)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
            LOGGER.debug("Trying to connect to couchbase bucket [{}]", this.bucketName);
            if (StringUtils.isBlank(this.bucketPassword)) {
                this.bucket = this.cluster.openBucket(this.bucketName, this.timeout, TimeUnit.MILLISECONDS);
            } else {
                this.bucket = this.cluster.openBucket(this.bucketName, this.bucketPassword, this.timeout, TimeUnit.MILLISECONDS);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to connect to Couchbase bucket " + this.bucketName, e);
        }
        LOGGER.info("Connected to Couchbase bucket [{}]", this.bucketName);
    }
}

