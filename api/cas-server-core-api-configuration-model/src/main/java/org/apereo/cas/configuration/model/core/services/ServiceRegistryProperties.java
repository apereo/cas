package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.model.support.aws.AmazonS3ServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.cassandra.serviceregistry.CassandraServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.cosmosdb.CosmosDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.couchbase.serviceregistry.CouchbaseServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.couchdb.serviceregistry.CouchDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.git.services.GitServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.model.support.redis.RedisServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.yaml.YamlServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-services", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ServiceRegistryProperties")
public class ServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = -368826011744304210L;

    /**
     * Properties pertaining to Cosmos DB service registry.
     */
    @NestedConfigurationProperty
    private CosmosDbServiceRegistryProperties cosmosDb = new CosmosDbServiceRegistryProperties();

    /**
     * Properties pertaining to Apache Cassandra service registry.
     */
    @NestedConfigurationProperty
    private CassandraServiceRegistryProperties cassandra = new CassandraServiceRegistryProperties();

    /**
     * Properties pertaining to Git-based service registry.
     */
    @NestedConfigurationProperty
    private GitServiceRegistryProperties git = new GitServiceRegistryProperties();

    /**
     * Properties pertaining to Cosmos DB service registry.
     */
    @NestedConfigurationProperty
    private CouchDbServiceRegistryProperties couchDb = new CouchDbServiceRegistryProperties();

    /**
     * Properties pertaining to REST service registry.
     */
    @NestedConfigurationProperty
    private RestfulServiceRegistryProperties rest = new RestfulServiceRegistryProperties();

    /**
     * Properties pertaining to redis service registry.
     */
    @NestedConfigurationProperty
    private RedisServiceRegistryProperties redis = new RedisServiceRegistryProperties();

    /**
     * Properties pertaining to JSON service registry.
     */
    @NestedConfigurationProperty
    private JsonServiceRegistryProperties json = new JsonServiceRegistryProperties();

    /**
     * Properties pertaining to YAML service registry.
     */
    @NestedConfigurationProperty
    private YamlServiceRegistryProperties yaml = new YamlServiceRegistryProperties();

    /**
     * Properties pertaining to jpa service registry.
     */
    @NestedConfigurationProperty
    private JpaServiceRegistryProperties jpa = new JpaServiceRegistryProperties();

    /**
     * Properties pertaining to ldap service registry.
     */
    @NestedConfigurationProperty
    private LdapServiceRegistryProperties ldap = new LdapServiceRegistryProperties();

    /**
     * Properties pertaining to mongo db service registry.
     */
    @NestedConfigurationProperty
    private MongoDbServiceRegistryProperties mongo = new MongoDbServiceRegistryProperties();

    /**
     * Properties pertaining to couchbase service registry.
     */
    @NestedConfigurationProperty
    private CouchbaseServiceRegistryProperties couchbase = new CouchbaseServiceRegistryProperties();

    /**
     * Properties pertaining to dynamo db service registry.
     */
    @NestedConfigurationProperty
    private DynamoDbServiceRegistryProperties dynamoDb = new DynamoDbServiceRegistryProperties();

    /**
     * Properties pertaining to amazon s3 service registry.
     */
    @NestedConfigurationProperty
    private AmazonS3ServiceRegistryProperties amazonS3 = new AmazonS3ServiceRegistryProperties();

    /**
     * Properties pertaining to streaming service registry content over the wire.
     */
    @NestedConfigurationProperty
    private StreamingServiceRegistryProperties stream = new StreamingServiceRegistryProperties();

    /**
     * Scheduler settings to indicate how often is metadata reloaded.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    /**
     * Registry caching settings.
     */
    @NestedConfigurationProperty
    private ServiceRegistryCacheProperties cache = new ServiceRegistryCacheProperties();

    /**
     * Registry core/common settings.
     */
    @NestedConfigurationProperty
    private ServiceRegistryCoreProperties core = new ServiceRegistryCoreProperties();
}
