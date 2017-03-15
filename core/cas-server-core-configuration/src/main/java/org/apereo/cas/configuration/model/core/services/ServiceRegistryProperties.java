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

    @NestedConfigurationProperty
    private JpaServiceRegistryProperties jpa = new JpaServiceRegistryProperties();

    @NestedConfigurationProperty
    private LdapServiceRegistryProperties ldap = new LdapServiceRegistryProperties();

    @NestedConfigurationProperty
    private MongoServiceRegistryProperties mongo = new MongoServiceRegistryProperties();

    @NestedConfigurationProperty
    private CouchbaseServiceRegistryProperties couchbase = new CouchbaseServiceRegistryProperties();

    @NestedConfigurationProperty
    private DynamoDbServiceRegistryProperties dynamoDb = new DynamoDbServiceRegistryProperties();
    
    private boolean initFromJson;

    private String startDelay = "PT15S";

    private String repeatInterval = "PT2M";

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
