package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.model.support.couchbase.serviceregistry.CouchbaseServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class ServiceRegistryProperties extends AbstractConfigProperties {

    private static final long serialVersionUID = -368826011744304210L;

    @NestedConfigurationProperty
    /**
     * Properties pertaining to jpa service registry.
     */
    private JpaServiceRegistryProperties jpa = new JpaServiceRegistryProperties();

    @NestedConfigurationProperty
    /**
     * Properties pertaining to ldap service registry.
     */
    private LdapServiceRegistryProperties ldap = new LdapServiceRegistryProperties();

    @NestedConfigurationProperty
    /**
     * Properties pertaining to mongo db service registry.
     */
    private MongoServiceRegistryProperties mongo = new MongoServiceRegistryProperties();

    @NestedConfigurationProperty
    /**
     * Properties pertaining to couchbase service registry.
     */
    private CouchbaseServiceRegistryProperties couchbase = new CouchbaseServiceRegistryProperties();

    @NestedConfigurationProperty
    /**
     * Properties pertaining to dynamo db service registry.
     */
    private DynamoDbServiceRegistryProperties dynamoDb = new DynamoDbServiceRegistryProperties();

    /**
     * Flag that indicates whether to initialise active service registry implementation with a default set of service definition included
     * with CAS in JSON format.
     */
    private boolean initFromJson;

    /**
     * String representation of a start delay of loading service definitions data for an active service registry implementation.
     */
    private String startDelay = "PT15S";

    /**
     * String representation of a repeat interval of re-loading service definitions data for an active service registry implementation.
     */
    private String repeatInterval = "PT2M";

    /**
     * Flag indicating whether a background watcher thread is enabled for the purposes of ;ive reloading of service registry data changes
     * from persistent data store.
     */
    private boolean watcherEnabled = true;

    /**
     * Instantiates a new Service registry properties.
     */
    public ServiceRegistryProperties() {
        super.getConfig().setLocation(new ClassPathResource("services"));
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
}
