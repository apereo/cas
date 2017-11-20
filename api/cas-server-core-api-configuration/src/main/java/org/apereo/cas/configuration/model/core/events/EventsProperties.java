package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.influxdb.InfluxDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-events", automated = true)
public class EventsProperties implements Serializable {

    private static final long serialVersionUID = 1734523424737956370L;
    /**
     * Whether geolocation should be tracked as part of collected authentication events.
     * This of course require's consent from the user's browser to collect stats on location.
     */
    private boolean trackGeolocation;

    /**
     * Whether CAS should track the underlying configuration store for changes.
     * This depends on whether the store provides that sort of functionality.
     * When running in standalone mode, this typically translates to CAS monitoring
     * configuration files and reloading context conditionally if there are any changes.
     */
    private boolean trackConfigurationModifications = true;

    /**
     * Track authentication events inside a database.
     */
    private Jpa jpa = new Jpa();
    /**
     * Track authentication events inside an influxdb database.
     */
    private InfluxDb influxDb = new InfluxDb();
    /**
     * Track authentication events inside a mongodb instance.
     */
    private MongoDb mongo = new MongoDb();

    public InfluxDb getInfluxDb() {
        return influxDb;
    }

    public void setInfluxDb(final InfluxDb influxDb) {
        this.influxDb = influxDb;
    }

    public MongoDb getMongo() {
        return mongo;
    }

    public void setMongo(final MongoDb mongodb) {
        this.mongo = mongodb;
    }

    public boolean isTrackGeolocation() {
        return trackGeolocation;
    }

    public void setTrackGeolocation(final boolean trackGeolocation) {
        this.trackGeolocation = trackGeolocation;
    }

    public boolean isTrackConfigurationModifications() {
        return trackConfigurationModifications;
    }

    public void setTrackConfigurationModifications(final boolean trackConfigurationModifications) {
        this.trackConfigurationModifications = trackConfigurationModifications;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    @RequiresModule(name = "cas-server-support-events-jpa")
    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 7647381223153797806L;
    }

    @RequiresModule(name = "cas-server-support-events-mongo")
    public static class MongoDb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = -1918436901491275547L;

        public MongoDb() {
            setCollection("MongoDbCasEventRepository");
        }
    }

    @RequiresModule(name = "cas-server-support-events-influxdb")
    public static class InfluxDb extends InfluxDbProperties {
        private static final long serialVersionUID = -3918436901491275547L;

        public InfluxDb() {
            setDatabase("CasInfluxDbEvents");
        }
    }
}
