package org.apereo.cas.configuration.model.support.mongo;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link MongoDbConnectionPoolProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-mongo-core")
public class MongoDbConnectionPoolProperties implements Serializable {
    private static final long serialVersionUID = 8312213511918496060L;

    /**
     * The maximum time a pooled connection can live for.  A zero value indicates no limit to the life time.  A pooled connection that
     * has exceeded its life time will be closed and replaced when necessary by a new connection.
     */
    private int lifeTime = 60_000;

    /**
     * The maximum idle time of a pooled connection.  A zero value indicates no limit to the idle time.  A pooled connection that has
     * exceeded its idle time will be closed and replaced when necessary by a new connection.
     */
    private int idleTime = 30_000;

    /**
     * The maximum time that a thread may wait for a connection to become available.
     */
    private int maxWaitTime = 60_000;

    /**
     * Maximum number of connections to keep around.
     */
    private int maxSize = 10;
    
    /**
     * Minimum number of connections to keep around.
     */
    private int minSize = 1;

    /**
     * Total number of connections allowed per host.
     */
    private int perHost = 10;
}
