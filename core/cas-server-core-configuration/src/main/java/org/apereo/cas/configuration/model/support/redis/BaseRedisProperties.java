package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import java.util.List;

/**
 * This is {@link BaseRedisProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class BaseRedisProperties implements Serializable {
    private static final long serialVersionUID = -2600996981339638782L;

    /**
     * Database index used by the connection factory.
     */
    @RequiredProperty
    private int database;

    /**
     * Redis server host.
     */
    @RequiredProperty
    private String host = "localhost";

    /**
     * Login password of the redis server.
     */
    @RequiredProperty
    private String password;

    /**
     * Redis server port.
     */
    @RequiredProperty
    private int port = 6379;

    /**
     * Connection timeout in milliseconds.
     */
    private int timeout = 2000;

    /**
     * Radius connection pool settings.
     */
    private Pool pool;

    /**
     * Redis Sentinel settings.
     */
    private Sentinel sentinel;

    /**
     * Whether or not to activate the pool configuration.
     */
    private boolean usePool = true;

    /**
     * Whether or not to use SSL for connection factory.
     */
    private boolean useSsl;

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(final int database) {
        this.database = database;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public boolean isUsePool() {
        return usePool;
    }

    public void setUsePool(final boolean usePool) {
        this.usePool = usePool;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(final boolean useSsl) {
        this.useSsl = useSsl;
    }

    public Pool getPool() {
        return this.pool;
    }

    public void setPool(final Pool pool) {
        this.pool = pool;
    }

    public Sentinel getSentinel() {
        return sentinel;
    }

    public void setSentinel(final Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    /**
     * Pool properties.
     */
    public static class Pool implements Serializable {

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

        public int getMaxIdle() {
            return this.maxIdle;
        }

        public void setMaxIdle(final int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return this.minIdle;
        }

        public void setMinIdle(final int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return this.maxActive;
        }

        public void setMaxActive(final int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return this.maxWait;
        }

        public void setMaxWait(final int maxWait) {
            this.maxWait = maxWait;
        }

        public boolean isLifo() {
            return lifo;
        }

        public void setLifo(final boolean lifo) {
            this.lifo = lifo;
        }

        public boolean isFairness() {
            return fairness;
        }

        public void setFairness(final boolean fairness) {
            this.fairness = fairness;
        }

        public boolean isTestOnCreate() {
            return testOnCreate;
        }

        public void setTestOnCreate(final boolean testOnCreate) {
            this.testOnCreate = testOnCreate;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(final boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public boolean isTestOnReturn() {
            return testOnReturn;
        }

        public void setTestOnReturn(final boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
        }

        public boolean isTestWhileIdle() {
            return testWhileIdle;
        }

        public void setTestWhileIdle(final boolean testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
        }

        public int getNumTestsPerEvictionRun() {
            return numTestsPerEvictionRun;
        }

        public void setNumTestsPerEvictionRun(final int numTestsPerEvictionRun) {
            this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        }

        public long getSoftMinEvictableIdleTimeMillis() {
            return softMinEvictableIdleTimeMillis;
        }

        public void setSoftMinEvictableIdleTimeMillis(final long softMinEvictableIdleTimeMillis) {
            this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
        }

        public long getMinEvictableIdleTimeMillis() {
            return minEvictableIdleTimeMillis;
        }

        public void setMinEvictableIdleTimeMillis(final long minEvictableIdleTimeMillis) {
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        }
    }

    /**
     * Redis sentinel properties.
     */
    public static class Sentinel implements Serializable {

        private static final long serialVersionUID = 5434823157764550831L;

        /**
         * Name of Redis server.
         */
        private String master;

        /**
         * list of host:port pairs.
         */
        private List<String> node;

        public String getMaster() {
            return this.master;
        }

        public void setMaster(final String master) {
            this.master = master;
        }

        public List<String> getNode() {
            return node;
        }

        public void setNode(final List<String> node) {
            this.node = node;
        }
    }

}
