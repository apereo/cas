package org.apereo.cas.configuration.model.support;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ConnectionPoolingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-util", automated = true)
public class ConnectionPoolingProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -5307463292890944799L;

    /**
     * Controls the minimum size that the pool is allowed
     * to reach, including both idle and in-use connections.
     */
    private int minSize = 6;

    /**
     * Controls the maximum number of connections to keep
     * in the pool, including both idle and in-use connections.
     */
    private int maxSize = 18;

    /**
     * Sets the maximum time in seconds that this data source will wait
     * while attempting to connect to a database.
     * <p>
     * A value of zero specifies that the timeout is the default system timeout
     * if there is one; otherwise, it specifies that there is no timeout.
     */
    @DurationCapable
    private String maxWait = "PT2S";

    /**
     * This property controls the keepalive interval for a connection in the pool. An in-use connection
     * will never be tested by the keepalive thread, only when it is idle will it be tested.
     * Default is zero, which disables this feature.
     */
    @DurationCapable
    private String keepAliveTime = "0";

    /**
     * This property controls the maximum lifetime of a connection in the pool. When a connection
     * reaches this timeout, even if recently used,
     * it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
     */
    @DurationCapable
    private String maximumLifetime = "PT10M";

    /**
     * Whether or not pool suspension is allowed.
     * <p>
     * There is a performance impact when pool suspension is enabled.
     * Unless you need it (for a redundancy system for example) do not enable it.
     */
    private boolean suspension;

    /**
     * The maximum number of milliseconds that the
     * pool will wait for a connection to be validated as alive.
     */
    private long timeoutMillis = 1_000L;

    /**
     * Set the name of the connection pool. This is primarily used for
     * the MBean to uniquely identify the pool configuration.
     */
    @Nullable
    private String name;
}
