package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.couchdb.ticketregistry.CouchDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ehcache.Ehcache3Properties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.model.support.infinispan.InfinispanProperties;
import org.apereo.cas.configuration.model.support.jms.JmsTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link TicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
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
    private MongoDbTicketRegistryProperties mongo = new MongoDbTicketRegistryProperties();

    /**
     * Ehcache registry settings.
     */
    @NestedConfigurationProperty
    private EhcacheProperties ehcache = new EhcacheProperties();

    /**
     * Ehcache3 registry settings.
     */
    @NestedConfigurationProperty
    private Ehcache3Properties ehcache3 = new Ehcache3Properties();


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
     * Cassandra registry settings.
     */
    @NestedConfigurationProperty
    private CassandraTicketRegistryProperties cassandra = new CassandraTicketRegistryProperties();

    /**
     * Settings relevant for the default in-memory ticket registry.
     */
    @NestedConfigurationProperty
    private InMemoryTicketRegistryProperties inMemory = new InMemoryTicketRegistryProperties();

    /**
     * CouchDb registry settings.
     */
    @NestedConfigurationProperty
    private CouchDbTicketRegistryProperties couchDb = new CouchDbTicketRegistryProperties();

    /**
     * Ticket registry cleaner settings.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT10S", "PT1M");

}
