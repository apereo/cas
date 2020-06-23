package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-events", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class EventsProperties implements Serializable {

    private static final long serialVersionUID = 1734523424737956370L;

    /**
     * Whether event tracking and recording functionality should be enabled.
     */
    private boolean enabled = true;

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
    @NestedConfigurationProperty
    private JpaEventsProperties jpa = new JpaEventsProperties();

    /**
     * Track authentication events inside an influxdb database.
     */
    @NestedConfigurationProperty
    private InfluxDbEventsProperties influxDb = new InfluxDbEventsProperties();

    /**
     * Track authentication events inside a mongodb instance.
     */
    @NestedConfigurationProperty
    private MongoDbEventsProperties mongo = new MongoDbEventsProperties();

    /**
     * Track authentication events inside a couchdb instance.
     */
    @NestedConfigurationProperty
    private CouchDbEventsProperties couchDb = new CouchDbEventsProperties();

    /**
     * Track authentication events inside a DynamoDb instance.
     */
    @NestedConfigurationProperty
    private DynamoDbEventsProperties dynamoDb = new DynamoDbEventsProperties();
    
}
