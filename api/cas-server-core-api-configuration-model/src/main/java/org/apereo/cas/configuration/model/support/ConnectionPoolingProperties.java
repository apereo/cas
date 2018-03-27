package org.apereo.cas.configuration.model.support;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ConnectionPoolingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class ConnectionPoolingProperties implements Serializable {

    private static final long serialVersionUID = -5307463292890944799L;

    /**
     * Controls the maximum size that the pool is allowed to reach, including both idle and in-use connections.
     */
    private int minSize = 6;

    /**
     * Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
     */
    private int maxSize = 18;

    /**
     * Sets the maximum time in seconds that this data source will wait
     * while attempting to connect to a database.
     *
     * A value of zero specifies that the timeout is the default system timeout
     * if there is one; otherwise, it specifies that there is no timeout.
     */
    private String maxWait = "PT2S";

    /**
     * Whether or not pool suspension is allowed.
     *
     * There is a performance impact when pool suspension is enabled.
     * Unless you need it (for a redundancy system for example) do not enable it.
     */
    private boolean suspension;

    /**
     * The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
     */
    private long timeoutMillis = 1_000;
}
