package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.model.support.infinispan.InfinispanProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link TicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class TicketRegistryProperties {

    @NestedConfigurationProperty
    private DynamoDbTicketRegistryProperties dynamoDb = new DynamoDbTicketRegistryProperties();
    
    @NestedConfigurationProperty
    private InfinispanProperties infinispan = new InfinispanProperties();

    @NestedConfigurationProperty
    private CouchbaseTicketRegistryProperties couchbase = new CouchbaseTicketRegistryProperties();

    @NestedConfigurationProperty
    private MongoTicketRegistryProperties mongo = new MongoTicketRegistryProperties();

    @NestedConfigurationProperty
    private EhcacheProperties ehcache = new EhcacheProperties();

    @NestedConfigurationProperty
    private HazelcastProperties hazelcast = new HazelcastProperties();

    @NestedConfigurationProperty
    private IgniteProperties ignite = new IgniteProperties();

    @NestedConfigurationProperty
    private JpaTicketRegistryProperties jpa = new JpaTicketRegistryProperties();

    @NestedConfigurationProperty
    private MemcachedTicketRegistryProperties memcached = new MemcachedTicketRegistryProperties();

    @NestedConfigurationProperty
    private RedisTicketRegistryProperties redis = new RedisTicketRegistryProperties();

    private InMemory inMemory = new InMemory();
    private Cleaner cleaner = new Cleaner();

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

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
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

    public HazelcastProperties getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(final HazelcastProperties hazelcast) {
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

    public static class InMemory {
        private int initialCapacity = 1000;
        private int loadFactor = 1;
        private int concurrency = 20;

        @NestedConfigurationProperty
        private CryptographyProperties crypto = new CryptographyProperties();

        public CryptographyProperties getCrypto() {
            return crypto;
        }

        public void setCrypto(final CryptographyProperties crypto) {
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
    }

    public static class Cleaner {
        private boolean enabled = true;
        private String startDelay = "PT10S";
        private String repeatInterval = "PT1M";

        private String appId = "cas-ticket-registry-cleaner";

        public String getAppId() {
            return appId;
        }

        public void setAppId(final String appId) {
            this.appId = appId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getStartDelay() {
            return Beans.newDuration(startDelay).toMillis();
        }

        public void setStartDelay(final String startDelay) {
            this.startDelay = startDelay;
        }

        public long getRepeatInterval() {
            return Beans.newDuration(repeatInterval).toMillis();
        }

        public void setRepeatInterval(final String repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }
}
