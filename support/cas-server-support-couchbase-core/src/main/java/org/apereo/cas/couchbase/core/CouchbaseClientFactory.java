package org.apereo.cas.couchbase.core;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
public class CouchbaseClientFactory {


    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseClientFactory.class);
    private static final int DEFAULT_TIMEOUT = 5;

    private Cluster cluster;
    private Bucket bucket;
    private final Collection<View> views;
    private final Set<String> nodes;

    /* The name of the getBucket, will use the default getBucket unless otherwise specified. */
    private String bucketName = "default";

    /* Password for the getBucket if any. */
    private String bucketPassword = StringUtils.EMPTY;

    /* Design document and views to create in the getBucket, if any. */
    private final String designDocument;

    private long timeout = DEFAULT_TIMEOUT;

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

        this.cluster = CouchbaseCluster.create(new ArrayList<>(this.nodes));

        this.designDocument = documentName;
        this.views = views;
    }

    /**
     * Instantiates a new Couchbase client factory.
     *
     * @param nodes          the nodes
     * @param bucketName     the bucket name
     * @param bucketPassword the bucket password
     */
    public CouchbaseClientFactory(final Set<String> nodes, final String bucketName, final String bucketPassword) {
        this(nodes, bucketName, bucketPassword, DEFAULT_TIMEOUT, null, null);
    }

    /**
     * Authenticate.
     *
     * @param uid the uid
     * @param psw the psw
     */
    public void authenticate(final String uid, final String psw) {
        this.cluster = this.cluster.authenticate(uid, psw);
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    /**
     * Inverse of connectBucket, shuts down the client, cancelling connection
     * task if not completed.
     */
    public void shutdown() {
        try {
            if (this.cluster != null) {
                this.cluster.disconnect();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve the Couchbase getBucket.
     *
     * @return the getBucket.
     */
    public Bucket getBucket() {
        if (this.bucket == null) {
            if (StringUtils.isBlank(this.bucketName)) {
                throw new IllegalArgumentException("Bucket name cannot be blank");
            }

            try {
                LOGGER.debug("Trying to connect to couchbase getBucket [{}]", this.bucketName);
                this.bucket = this.cluster.openBucket(this.bucketName, this.bucketPassword, this.timeout, TimeUnit.SECONDS);
                LOGGER.info("Connected to Couchbase getBucket [{}]", this.bucketName);
                if (this.views != null && this.designDocument != null) {
                    LOGGER.debug("Ensure that indexes exist in getBucket [{}]", this.bucket.name());
                    final DesignDocument newDocument = DesignDocument.create(this.designDocument, new ArrayList<>(views));
                    if (!newDocument.equals(this.bucket.bucketManager().getDesignDocument(this.designDocument))) {
                        LOGGER.warn("Missing indexes in getBucket [{}] for document [{}]", this.bucket.name(), this.designDocument);
                        this.bucket.bucketManager().upsertDesignDocument(newDocument);
                    }
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException("Failed to connect to Couchbase getBucket", e);
            }
        }
        return this.bucket;
    }
}

