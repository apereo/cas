package org.apereo.cas.configuration.model.support.couchbase;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<String> addresses = Stream.of("localhost").collect(Collectors.toList());

    /**
     * String representation of connection timeout.
     */
    @DurationCapable
    private String connectionTimeout = "PT60S";

    /**
     * String representation of idle connection timeout.
     */
    @DurationCapable
    private String idleConnectionTimeout = "PT60S";

    /**
     * String representation of search timeout.
     */
    @DurationCapable
    private String searchTimeout = "PT30S";

    /**
     * String representation of query timeout.
     */
    @DurationCapable
    private String queryTimeout = "PT30S";

    /**
     * String representation of view timeout.
     */
    @DurationCapable
    private String viewTimeout = "PT30S";

    /**
     * String representation of KV timeout.
     */
    @DurationCapable
    private String kvTimeout = "PT30S";

    /**
     * String representation of scan timeout.
     */
    @DurationCapable
    private String scanWaitTimeout = "PT30S";

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
     * Maximum number of parallel threads made for queries.
     */
    private int maxParallelism;

    /**
     * Allows to customize the maximum number of
     * requests allowed in the retry timer.
     */
    private long maxNumRequestsInRetry = 32768;

    /**
     * Bucket name.
     */
    @RequiredProperty
    private String bucket = "testbucket";

    /**
     * Query scan consistency.
     *
     * By default, the query engine will return whatever is currently in the index at
     * the time of query (this mode is also called {@code NOT_BOUNDED}). If you
     * need to include everything that has just been written, a different scan consistency must
     * be chosen. If {@code REQUEST_PLUS} is chosen, it will likely take a bit
     * longer to return the results but the query engine will make sure that it is as up-to-date as possible.
     *
     * Accepted values are: {@code NOT_BOUNDED, REQUEST_PLUS}.
     */
    private String scanConsistency = "NOT_BOUNDED";
}
