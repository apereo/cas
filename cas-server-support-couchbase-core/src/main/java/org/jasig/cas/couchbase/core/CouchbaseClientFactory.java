package org.jasig.cas.couchbase.core;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
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
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Cluster cluster;
    private Bucket bucket;
    private List<View> views;

    @NotNull
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
            logger.debug("Trying to connect to couchbase bucket {}", bucketName);

            cluster = CouchbaseCluster.create(new ArrayList<>(nodes));

            bucket = cluster.openBucket(bucketName, password, timeout, TimeUnit.SECONDS);

            logger.info("Connected to Couchbase bucket {}.", bucketName);

            if (views != null) {
                doEnsureIndexes(designDocument, views);
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
            if (cluster != null) {
                cluster.disconnect();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the Couchbase bucket.
     *
     * @return the bucket.
     */
    public Bucket bucket() {
        if (bucket != null) {
            return bucket;
        }
        throw new RuntimeException("Connection to bucket " + bucketName + " not initialized yet.");
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
        logger.debug("Ensure that indexes exist in bucket {}.", bucket.name());
        final DesignDocument newDocument = DesignDocument.create(documentName, views);
        if (!newDocument.equals(bucket.bucketManager().getDesignDocument(documentName))) {
            logger.warn("Missing indexes in bucket {} for document {}, creating new.", bucket.name(), documentName);
            bucket.bucketManager().upsertDesignDocument(newDocument);
        }
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public void setNodes(final Set<String> nodes) {
        this.nodes = nodes;
    }

    public void setBucket(final String bucket) {
        this.bucketName = bucket;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}

