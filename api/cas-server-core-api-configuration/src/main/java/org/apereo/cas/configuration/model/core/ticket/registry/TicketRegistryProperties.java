package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.model.support.infinispan.InfinispanProperties;
import org.apereo.cas.configuration.model.support.jms.JmsTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link TicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
public class TicketRegistryProperties implements Serializable {

    private static final long serialVersionUID = -4735458476452635679L;

    /**
     * JMS registry settings.
     */
    @NestedConfigurationProperty
    private JmsTicketRegistryProperties jms = new JmsTicketRegistryProperties();
    
    /**
     * DynamoDb registry settings.
     */
    @NestedConfigurationProperty
    private DynamoDbTicketRegistryProperties dynamoDb = new DynamoDbTicketRegistryProperties();

    /**
     * Infinispan registry settings.
     */
    @NestedConfigurationProperty
    private InfinispanProperties infinispan = new InfinispanProperties();

    /**
     * Couchbase registry settings.
     */
    @NestedConfigurationProperty
    private CouchbaseTicketRegistryProperties couchbase = new CouchbaseTicketRegistryProperties();

    /**
     * MongoDb registry settings.
     */
    @NestedConfigurationProperty
    private MongoTicketRegistryProperties mongo = new MongoTicketRegistryProperties();

    /**
     * Ehcache registry settings.
     */
    @NestedConfigurationProperty
    private EhcacheProperties ehcache = new EhcacheProperties();

    /**
     * Hazelcast registry settings.
     */
    @NestedConfigurationProperty
    private HazelcastTicketRegistryProperties hazelcast = new HazelcastTicketRegistryProperties();

    /**
     * Apache Ignite registry settings.
     */
    @NestedConfigurationProperty
    private IgniteProperties ignite = new IgniteProperties();

    /**
     * JPA registry settings.
     */
    @NestedConfigurationProperty
    private JpaTicketRegistryProperties jpa = new JpaTicketRegistryProperties();

    /**
     * Memcached registry settings.
     */
    @NestedConfigurationProperty
    private MemcachedTicketRegistryProperties memcached = new MemcachedTicketRegistryProperties();

    /**
     * Redis registry settings.
     */
    @NestedConfigurationProperty
    private RedisTicketRegistryProperties redis = new RedisTicketRegistryProperties();

    /**
     * Settings relevant for the default in-memory ticket registry.
     */
    private InMemory inMemory = new InMemory();

    /**
     * Ticket registry cleaner settings.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT10S", "PT1M");

    public MongoTicketRegistryProperties getMongo() {
        return mongo;
    }

    public void setMongo(final MongoTicketRegistryProperties mongo) {
        this.mongo = mongo;
    }

    public InMemory getInMemory() {
        return inMemory;
    }

    public void setInMemory(final InMemory inMemory) {
        this.inMemory = inMemory;
    }

    public ScheduledJobProperties getCleaner() {
        return cleaner;
    }

    public void setCleaner(final ScheduledJobProperties cleaner) {
        this.cleaner = cleaner;
    }

    public CouchbaseTicketRegistryProperties getCouchbase() {
        return couchbase;
    }

    public void setCouchbase(final CouchbaseTicketRegistryProperties couchbase) {
        this.couchbase = couchbase;
    }

    public EhcacheProperties getEhcache() {
        return ehcache;
    }

    public void setEhcache(final EhcacheProperties ehcache) {
        this.ehcache = ehcache;
    }

    public HazelcastTicketRegistryProperties getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(final HazelcastTicketRegistryProperties hazelcast) {
        this.hazelcast = hazelcast;
    }

    public IgniteProperties getIgnite() {
        return ignite;
    }

    public void setIgnite(final IgniteProperties ignite) {
        this.ignite = ignite;
    }

    public JpaTicketRegistryProperties getJpa() {
        return jpa;
    }

    public void setJpa(final JpaTicketRegistryProperties jpa) {
        this.jpa = jpa;
    }

    public MemcachedTicketRegistryProperties getMemcached() {
        return memcached;
    }

    public void setMemcached(final MemcachedTicketRegistryProperties memcached) {
        this.memcached = memcached;
    }

    public InfinispanProperties getInfinispan() {
        return infinispan;
    }

    public void setInfinispan(final InfinispanProperties infinispan) {
        this.infinispan = infinispan;
    }

    public RedisTicketRegistryProperties getRedis() {
        return redis;
    }

    public void setRedis(final RedisTicketRegistryProperties redis) {
        this.redis = redis;
    }

    public DynamoDbTicketRegistryProperties getDynamoDb() {
        return dynamoDb;
    }
                                        
    public void setDynamoDb(final DynamoDbTicketRegistryProperties dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public JmsTicketRegistryProperties getJms() {
        return jms;
    }

    public void setJms(final JmsTicketRegistryProperties jms) {
        this.jms = jms;
    }

    @RequiresModule(name = "cas-server-core-tickets", automated = true)
    public static class InMemory implements Serializable {

        private static final long serialVersionUID = -2600525447128979994L;

        /**
         * Allow the ticket registry to cache ticket items for period of time
         * and auto-evict and clean up, removing the need to running a ticket
         * registry cleaner in the background.
         */
        private boolean cache;
        
        /**
         * The initial capacity of the underlying memory store.
         * The implementation performs internal sizing to accommodate this many elements.
         */
        private int initialCapacity = 1000;

        /**
         *  The load factor threshold, used to control resizing.
         *  Resizing may be performed when the average number of elements per bin exceeds this threshold.
         */
        private int loadFactor = 1;

        /**
         * The estimated number of concurrently updating threads.
         * The implementation performs internal sizing to try to accommodate this many threads.
         */
        private int concurrency = 20;

        /**
         * Crypto settings for the registry.
         */
        @NestedConfigurationProperty
        private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

        public InMemory() {
            crypto.setEnabled(false);
        }

        public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
            return crypto;
        }

        public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
            this.crypto = crypto;
        }

        public int getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(final int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public int getLoadFactor() {
            return loadFactor;
        }

        public void setLoadFactor(final int loadFactor) {
            this.loadFactor = loadFactor;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(final int concurrency) {
            this.concurrency = concurrency;
        }

        public boolean isCache() {
            return cache;
        }

        public void setCache(final boolean cache) {
            this.cache = cache;
        }
    }
}
