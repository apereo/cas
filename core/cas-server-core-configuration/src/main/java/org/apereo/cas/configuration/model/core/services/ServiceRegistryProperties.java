package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.model.support.couchbase.serviceregistry.CouchbaseServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.model.support.redis.RedisServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.yaml.YamlServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-services", automated = true)
public class ServiceRegistryProperties implements Serializable {

    private static final long serialVersionUID = -368826011744304210L;

    /**
     * Types of service managers that one can control.
     */
    public enum ServiceManagementTypes {
        /**
         * Group service definitions by their domain.
         */
        DOMAIN,
        /**
         * Default option to keep definitions in a map as they arrive.
         */
        DEFAULT
    }
    /**
     * Properties pertaining to REST service registry.
     */
    @NestedConfigurationProperty
    private BaseRestEndpointProperties rest = new BaseRestEndpointProperties();
    
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
    private MongoServiceRegistryProperties mongo = new MongoServiceRegistryProperties();

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
     * Flag that indicates whether to initialise active service registry implementation with a default set of service definition included
     * with CAS in JSON format.
     */
    private boolean initFromJson;

    /**
     * Flag indicating whether a background watcher thread is enabled for the purposes of live reloading of service registry data changes
     * from persistent data store.
     */
    private boolean watcherEnabled = true;

    /**
     * Determine how services are internally managed, queried, cached and reloaded by CAS.
     * Accepted values are the following:
     * 
     * <ul>
     *     <li>DEFAULT: Keep all services inside a concurrent map.</li>
     *     <li>DOMAIN: Group registered services by their domain having been explicitly defined.</li>
     * </ul>
     */
    private ServiceManagementTypes managementType = ServiceManagementTypes.DEFAULT;

    public ServiceManagementTypes getManagementType() {
        return managementType;
    }

    public void setManagementType(final ServiceManagementTypes managementType) {
        this.managementType = managementType;
    }

    public BaseRestEndpointProperties getRest() {
        return rest;
    }

    public void setRest(final BaseRestEndpointProperties rest) {
        this.rest = rest;
    }

    public boolean isInitFromJson() {
        return initFromJson;
    }

    public void setInitFromJson(final boolean initFromJson) {
        this.initFromJson = initFromJson;
    }

    public boolean isWatcherEnabled() {
        return watcherEnabled;
    }

    public void setWatcherEnabled(final boolean watcherEnabled) {
        this.watcherEnabled = watcherEnabled;
    }

    public JpaServiceRegistryProperties getJpa() {
        return jpa;
    }

    public void setJpa(final JpaServiceRegistryProperties jpa) {
        this.jpa = jpa;
    }

    public LdapServiceRegistryProperties getLdap() {
        return ldap;
    }

    public void setLdap(final LdapServiceRegistryProperties ldap) {
        this.ldap = ldap;
    }

    public MongoServiceRegistryProperties getMongo() {
        return mongo;
    }

    public void setMongo(final MongoServiceRegistryProperties mongo) {
        this.mongo = mongo;
    }

    public CouchbaseServiceRegistryProperties getCouchbase() {
        return couchbase;
    }

    public void setCouchbase(final CouchbaseServiceRegistryProperties couchbase) {
        this.couchbase = couchbase;
    }

    public DynamoDbServiceRegistryProperties getDynamoDb() {
        return dynamoDb;
    }

    public void setDynamoDb(final DynamoDbServiceRegistryProperties dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public JsonServiceRegistryProperties getJson() {
        return json;
    }

    public void setJson(final JsonServiceRegistryProperties json) {
        this.json = json;
    }

    public YamlServiceRegistryProperties getYaml() {
        return yaml;
    }

    public void setYaml(final YamlServiceRegistryProperties yaml) {
        this.yaml = yaml;
    }

    public StreamingServiceRegistryProperties getStream() {
        return stream;
    }

    public void setStream(final StreamingServiceRegistryProperties stream) {
        this.stream = stream;
    }

    public SchedulingProperties getSchedule() {
        return schedule;
    }

    public void setSchedule(final SchedulingProperties schedule) {
        this.schedule = schedule;
    }

    public RedisServiceRegistryProperties getRedis() {
        return redis;
    }

    public void setRedis(final RedisServiceRegistryProperties redis) {
        this.redis = redis;
    }

    public EmailProperties getMail() {
        return mail;
    }

    public void setMail(final EmailProperties mail) {
        this.mail = mail;
    }

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(final SmsProperties sms) {
        this.sms = sms;
    }
}
