package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 1734523424737956370L;

    /**
     * Core and common events settings.
     */
    @NestedConfigurationProperty
    private CoreEventsProperties core = new CoreEventsProperties();

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
     * Track authentication events inside a DynamoDb instance.
     */
    @NestedConfigurationProperty
    private DynamoDbEventsProperties dynamoDb = new DynamoDbEventsProperties();

    /**
     * Track authentication events inside a Redis instance.
     */
    @NestedConfigurationProperty
    private RedisEventsProperties redis = new RedisEventsProperties();

    /**
     * Track authentication events inside Kafka topics.
     */
    @NestedConfigurationProperty
    private KafkaEventsProperties kafka = new KafkaEventsProperties();
}
