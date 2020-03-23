package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link BaseHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
public class BaseHazelcastProperties implements Serializable {

    /**
     * Whether shut down hook is enabled.
     */
    public static final String SHUT_DOWN_HOOK_ENABLED_PROP = "hazelcast.shutdownhook.enabled";

    /**
     * Boolean value to indicate socket bind config.
     */
    public static final String SOCKET_BIND_ANY_PROP = "hazelcast.socket.bind.any";

    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";

    /**
     * Enable discovery.
     */
    public static final String HAZELCAST_DISCOVERY_ENABLED_PROP = "hazelcast.discovery.enabled";

    /**
     * It is an overrider property for the default server socket listener's IP address.
     * If this property is set, then this is the address where the server socket is bound to.
     */
    public static final String HAZELCAST_LOCAL_ADDRESS_PROP = "hazelcast.local.localAddress";

    /**
     * It is an overrider property for the default public address
     * to be advertised to other cluster members and clients.
     */
    public static final String HAZELCAST_PUBLIC_ADDRESS_PROP = "hazelcast.local.publicAddress";

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
     * Hazelcast enterprise license key.
     */
    private String licenseKey;

    /**
     * Enables compression when default java serialization is used.
     */
    private boolean enableCompression;

    /**
     * Hazelcast cluster settings if CAS is able to auto-create caches.
     */
    @NestedConfigurationProperty
    private HazelcastClusterProperties cluster = new HazelcastClusterProperties();
}
