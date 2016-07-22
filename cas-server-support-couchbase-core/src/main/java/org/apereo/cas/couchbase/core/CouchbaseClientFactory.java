package org.apereo.cas.couchbase.core;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import com.google.common.base.Throwables;
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
    private transient Logger logger = LoggerFactory.getLogger(getClass());

    private Cluster cluster;
    private Bucket bucket;
    private List<View> views;

    
    private Set<String> nodes;

    /* The name of the bucket, will use the default bucket unless otherwise specified. */
    private String bucketName = "default";

    /* Password for the bucket if any. */
    private String password = "";

    /* Design document and views to create in the bucket, if any. */
    private String designDocument;

    private long timeout = 5;

    /**
     * Instantiates a new Couchbase client factory.
     */
    public CouchbaseClientFactory() {}

    /**
     * Start initializing the client. This will schedule a task that retries
     * connection until successful.
     */
    public void initialize() {
        try {
            logger.debug("Trying to connect to couchbase bucket {}", this.bucketName);

            this.cluster = CouchbaseCluster.create(new ArrayList<>(this.nodes));

            this.bucket = this.cluster.openBucket(this.bucketName, this.password, this.timeout, TimeUnit.SECONDS);

            logger.info("Connected to Couchbase bucket {}.", this.bucketName);

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
        logger.debug("Ensure that indexes exist in bucket {}.", this.bucket.name());
        final DesignDocument newDocument = DesignDocument.create(documentName, views);
        if (!newDocument.equals(this.bucket.bucketManager().getDesignDocument(documentName))) {
            logger.warn("Missing indexes in bucket {} for document {}, creating new.", this.bucket.name(), documentName);
            this.bucket.bucketManager().upsertDesignDocument(newDocument);
        }
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public void setNodes(final Set<String> nodes) {
        this.nodes = nodes;
    }

    public void setBucketName(final String bucket) {
        this.bucketName = bucket;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}

