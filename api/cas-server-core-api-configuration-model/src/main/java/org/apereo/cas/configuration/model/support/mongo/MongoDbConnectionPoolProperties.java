package org.apereo.cas.configuration.model.support.mongo;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbConnectionPoolProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-mongo-core")
public class MongoDbConnectionPoolProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 8312213511918496060L;

    /**
     * The maximum time a pooled connection can live for.  A zero value indicates no limit
     * to the life time.  A pooled connection that
     * has exceeded its life time will be closed and replaced when necessary by a new connection.
     * Every replacement repeats the TLS handshake and authentication, so a short life time adds
     * latency under load; a finite value still lets connections move to new servers after topology or DNS changes.
     */
    @DurationCapable
    private String lifeTime = "PT30M";

    /**
     * The maximum idle time of a pooled connection.  A zero value indicates no limit
     * to the idle time.  A pooled connection that has
     * exceeded its idle time will be closed and replaced when necessary by a new connection.
     * Keep this below any idle timeout enforced by firewalls or load balancers between CAS and MongoDb.
     */
    @DurationCapable
    private String idleTime = "PT5M";

    /**
     * The maximum time that a thread may wait for a connection to become available.
     * When the pool is exhausted, the request fails once this time passes rather than holding the thread.
     */
    @DurationCapable
    private String maxWaitTime = "PT10S";

    /**
     * Maximum number of connections to keep around.
     * Connections are opened on demand up to this limit, and a single login makes several requests to the ticket registry.
     */
    private int maxSize = 100;

    /**
     * Minimum number of connections to keep around.
     */
    private int minSize = 1;
}
