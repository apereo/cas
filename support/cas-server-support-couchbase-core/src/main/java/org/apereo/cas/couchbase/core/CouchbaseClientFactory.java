package org.apereo.cas.couchbase.core;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A factory class which produces a client for a particular Couchbase bucket.
 * A design consideration was that we want the server to start even if Couchbase
 * is unavailable, picking up the connection when Couchbase comes online. Hence
 * the creation of the client is made using a scheduled task which is repeated
 * until successful connection is made.
 *
 * @author Fredrik Jönsson "fjo@kth.se"
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CouchbaseClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseClientFactory.class);
    
    private Cluster cluster;
    private Bucket bucket;
    private List<View> views;
    private final Set<String> nodes;

    /* The name of the bucket, will use the default bucket unless otherwise specified. */
    private String bucketName = "default";

    /* Password for the bucket if any. */
    private String password = StringUtils.EMPTY;

    /* Design document and views to create in the bucket, if any. */
    private String designDocument;

    private long timeout = 5;

    /**
     * Instantiates a new Couchbase client factory.
     * @param nodes cluster nodes
     * @param bucketName bucket name
     * @param password cluster password
     * @param timeout connection timeout
     */
    public CouchbaseClientFactory(final Set<String> nodes, final String bucketName, final String password, final long timeout) {
        this.nodes = nodes;
        this.bucketName = bucketName;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * Start initializing the client. This will schedule a task that retries
     * connection until successful.
     */
    public void initialize() {
        try {
            LOGGER.debug("Trying to connect to couchbase bucket [{}]", this.bucketName);

            this.cluster = CouchbaseCluster.create(new ArrayList<>(this.nodes));

            this.bucket = this.cluster.openBucket(this.bucketName, this.password, this.timeout, TimeUnit.SECONDS);

            LOGGER.info("Connected to Couchbase bucket [{}]", this.bucketName);

            if (this.views != null) {
                doEnsureIndexes(this.designDocument, this.views);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed to connect to Couchbase bucket", e);
        }
    }

    /**
     * Inverse of initialize, shuts down the client, cancelling connection
     * task if not completed.
     */
    public void shutdown() {
        try {
            if (this.cluster != null) {
                this.cluster.disconnect();
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Retrieve the Couchbase bucket.
     *
     * @return the bucket.
     */
    public Bucket bucket() {
        if (this.bucket != null) {
            return this.bucket;
        }
        throw new RuntimeException("Connection to bucket " + this.bucketName + " not initialized yet.");
    }

    /**
     * Register indexes to ensure in the bucket when the client is initialized.
     *
     * @param documentName name of the Couchbase design document.
     * @param views the list of Couchbase views (i.e. indexes) to create in the document.
     */
    public void ensureIndexes(final String documentName, final List<View> views) {
        this.designDocument = documentName;
        this.views = views;
    }

    /**
     * Ensures that all views exists in the database.
     *
     * @param documentName the name of the design document.
     * @param views the views to ensure exists in the database.
     */
    private void doEnsureIndexes(final String documentName, final List<View> views) {
        LOGGER.debug("Ensure that indexes exist in bucket [{}]", this.bucket.name());
        final DesignDocument newDocument = DesignDocument.create(documentName, views);
        if (!newDocument.equals(this.bucket.bucketManager().getDesignDocument(documentName))) {
            LOGGER.warn("Missing indexes in bucket [{}] for document [{}]", this.bucket.name(), documentName);
            this.bucket.bucketManager().upsertDesignDocument(newDocument);
        }
    }
}

