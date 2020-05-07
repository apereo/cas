package org.apereo.cas.configuration.model.support.couchbase;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-couchbase-core")
public abstract class BaseCouchbaseProperties implements Serializable {

    private static final long serialVersionUID = 6550895842866988551L;

    /**
     * Node addresses.
     */
    @RequiredProperty
    private String nodeSet = "localhost";

    /**
     * String representation of connection timeout.
     */
    private String connectionTimeout = "PT60S";

    /**
     * String representation of search timeout.
     */
    private String searchTimeout = "PT30S";

    /**
     * String representation of query timeout.
     */
    private String queryTimeout = "PT30S";

    /**
     * String representation of view timeout.
     */
    private String viewTimeout = "PT30S";

    /**
     * String representation of KV timeout.
     */
    private String kvTimeout = "PT30S";

    /**
     * Cluster username.
     */
    @RequiredProperty
    private String clusterUsername;

    /**
     * Cluster password.
     */
    @RequiredProperty
    private String clusterPassword;

    /**
     * Maximum number of connections made to the cluster.
     */
    private int maxHttpConnections = 5;

    /**
     * Bucket name.
     */
    @RequiredProperty
    private String bucket = "testbucket";
}
