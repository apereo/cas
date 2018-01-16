package org.apereo.cas.configuration.model.support.hazelcast;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Slf4j
@Getter
@Setter
public class BaseHazelcastProperties implements Serializable {

    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";

    /**
     * Enable discovery.
     */
    public static final String HAZELCAST_DISCOVERY_ENABLED = "hazelcast.discovery.enabled";

    /**
     * Max num of seconds for heartbeat property name.
     */
    public static final String MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds";

    /**
     * Ipv4 protocol stack.
     */
    public static final String IPV4_STACK_PROP = "hazelcast.prefer.ipv4.stack";

    private static final long serialVersionUID = 4204884717547468480L;

    /**
     * Hazelcast cluster settings if CAS is able to auto-create caches.
     */
    @NestedConfigurationProperty
    private HazelcastClusterProperties cluster = new HazelcastClusterProperties();
}
