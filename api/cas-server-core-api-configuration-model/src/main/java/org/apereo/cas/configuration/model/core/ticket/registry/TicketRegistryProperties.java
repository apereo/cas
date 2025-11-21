package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.gcp.GoogleCloudFirestoreTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.geode.GeodeProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.kafka.KafkaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoDbTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -4735458476452635679L;

    /**
     * CosmosDb registry settings.
     */
    @NestedConfigurationProperty
    private CosmosDbTicketRegistryProperties cosmosDb = new CosmosDbTicketRegistryProperties();

    /**
     * DynamoDb registry settings.
     */
    @NestedConfigurationProperty
    private DynamoDbTicketRegistryProperties dynamoDb = new DynamoDbTicketRegistryProperties();

    /**
     * MongoDb registry settings.
     */
    @NestedConfigurationProperty
    private MongoDbTicketRegistryProperties mongo = new MongoDbTicketRegistryProperties();

    /**
     * GoogleCloud Firestore registry settings.
     */
    @NestedConfigurationProperty
    private GoogleCloudFirestoreTicketRegistryProperties googleCloudFirestore = new GoogleCloudFirestoreTicketRegistryProperties();

    /**
     * Hazelcast registry settings.
     */
    @NestedConfigurationProperty
    private HazelcastTicketRegistryProperties hazelcast = new HazelcastTicketRegistryProperties();

    /**
     * Kafka registry settings.
     */
    @NestedConfigurationProperty
    private KafkaTicketRegistryProperties kafka = new KafkaTicketRegistryProperties();

    /**
     * Apache Ignite registry settings.
     */
    @NestedConfigurationProperty
    private IgniteProperties ignite = new IgniteProperties();

    /**
     * Apache Geode registry settings.
     */
    @NestedConfigurationProperty
    private GeodeProperties geode = new GeodeProperties();

    /**
     * JPA registry settings.
     */
    @NestedConfigurationProperty
    private JpaTicketRegistryProperties jpa = new JpaTicketRegistryProperties();

    /**
     * Memcached registry settings.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
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
     * Settings relevant for the default stateless ticket registry.
     */
    @NestedConfigurationProperty
    private StatelessTicketRegistryProperties stateless = new StatelessTicketRegistryProperties();

    /**
     * Ticket registry cleaner settings.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties();

    /**
     * Ticket registry core settings.
     */
    @NestedConfigurationProperty
    private TicketRegistryCoreProperties core = new TicketRegistryCoreProperties();
    
    public TicketRegistryProperties() {
        cleaner.getSchedule().setEnabled(true).setStartDelay("PT10S").setRepeatInterval("PT1M");
    }
}
