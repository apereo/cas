package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RedisPoolProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-redis-core")
@JsonFilter("RedisPoolProperties")
public class RedisPoolProperties implements Serializable {

    private static final long serialVersionUID = 8534823157764550894L;

    /**
     * Sets the maximum number of objects to examine during each run (if any) of the
     * idle object evictor thread. When positive, the number of tests performed for
     * a run will be the minimum of the configured value and the number of idle
     * instances in the pool. When negative, the number of tests performed will be
     * ceil(getNumIdle()/ abs(getNumTestsPerEvictionRun())) which means that when
     * the value is -n roughly one nth of the idle objects will be tested per run.
     */
    private int numTestsPerEvictionRun;

    /**
     * Sets the minimum amount of time an object may sit idle in the pool before it
     * is eligible for eviction by the idle object evictor (if any - see
     * setTimeBetweenEvictionRunsMillis(long)), with the extra condition that at
     * least minIdle object instances remain in the pool. This setting is overridden
     * by getMinEvictableIdleTimeMillis() (that is, if
     * getMinEvictableIdleTimeMillis() is positive, then
     * getSoftMinEvictableIdleTimeMillis() is ignored).
     */
    private long softMinEvictableIdleTimeMillis;

    /**
     * Sets the minimum amount of time an object may sit idle in the pool before it
     * is eligible for eviction by the idle object evictor (if any - see
     * setTimeBetweenEvictionRunsMillis(long)). When non-positive, no objects will
     * be evicted from the pool due to idle time alone.
     */
    private long minEvictableIdleTimeMillis;

    /**
     * Returns whether the pool has LIFO (last in, first out) behaviour with respect
     * to idle objects - always returning the most recently used object from the
     * pool, or as a FIFO (first in, first out) queue, where the pool always returns
     * the oldest object in the idle object pool.
     */
    private boolean lifo = true;

    /**
     * Returns whether or not the pool serves threads waiting to borrow objects
     * fairly. True means that waiting threads are served as if waiting in a FIFO
     * queue.
     */
    private boolean fairness;

    /**
     * Returns whether objects created for the pool will be validated before being
     * returned from the borrowObject() method. Validation is performed by the
     * validateObject() method of the factory associated with the pool. If the
     * object fails to validate, then borrowObject() will fail.
     */
    private boolean testOnCreate;

    /**
     * Returns whether objects borrowed from the pool will be validated before being
     * returned from the borrowObject() method. Validation is performed by the
     * validateObject() method of the factory associated with the pool. If the
     * object fails to validate, it will be removed from the pool and destroyed, and
     * a new attempt will be made to borrow an object from the pool.
     */
    private boolean testOnBorrow;

    /**
     * Returns whether objects borrowed from the pool will be validated when they
     * are returned to the pool via the returnObject() method. Validation is
     * performed by the validateObject() method of the factory associated with the
     * pool. Returning objects that fail validation are destroyed rather then being
     * returned the pool.
     */
    private boolean testOnReturn;

    /**
     * Returns whether objects sitting idle in the pool will be validated by the
     * idle object evictor ( if any - see setTimeBetweenEvictionRunsMillis(long)).
     * Validation is performed by the validateObject() method of the factory
     * associated with the pool. If the object fails to validate, it will be removed
     * from the pool and destroyed.
     */
    private boolean testWhileIdle;

    /**
     * Max number of "idle" connections in the pool. Use a negative value to
     * indicate an unlimited number of idle connections.
     */
    private int maxIdle = 8;

    /**
     * Target for the minimum number of idle connections to maintain in the pool.
     * This setting only has an effect if it is positive.
     */
    private int minIdle;

    /**
     * Max number of connections that can be allocated by the pool at a given time.
     * Use a negative value for no limit.
     */
    private int maxActive = 8;

    /**
     * Maximum amount of time (in milliseconds) a connection allocation should block
     * before throwing an exception when the pool is exhausted. Use a negative value
     * to block indefinitely.
     */
    private int maxWait = -1;

    /**
     * Enable the pooling configuration.
     */
    @RequiredProperty
    private boolean enabled;
}
