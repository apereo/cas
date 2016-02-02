package org.jasig.cas.ticket.registry.config;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Component
public class HazelcastProperties {

    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";

    /**
     * Max num of seconds for heartbeat property name.
     */
    public static final String MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds";

    /**
     * Map name.
     */
    @Value("${hz.mapname:tickets}")
    private String mapName;

    /**
     * Logging type.
     */
    @Value("${hz.cluster.logging.type:slf4j}")
    private String loggingType;

    /**
     * Max no of heartbeat seconds.
     */
    @Value("${hz.cluster.max.heartbeat.seconds:5}")
    private String maxNoHeartbeatSeconds;

    /**
     * Instance name.
     */
    @Value("${hz.cluster.instance.name:localhost}")
    private String instanceName;

    /**
     * Port auto increment.
     */
    @Value("${hz.cluster.portAutoIncrement:true}")
    private boolean portAutoIncrement;

    /**
     * Port.
     */
    @Value("${hz.cluster.port:5701}")
    private int port;

    /**
     * Multicast enabled.
     */
    @Value("${hz.cluster.multicast.enabled:false}")
    private boolean multicastEnabled;

    /**
     * TCP/IP enabled.
     */
    @Value("${hz.cluster.tcpip.enabled:true}")
    private boolean tcpipEnabled;

    /**
     * Members.
     */
    @Value("${hz.cluster.members:localhost}")
    private List<String> members;

    /**
     * Max heap size percentage.
     */
    @Value("${hz.cluster.max.heapsize.percentage:85}")
    private int maxHeapSizePercentage;

    /**
     * Max size policy.
     */
    @Value("${hz.cluster.max.size.policy:USED_HEAP_PERCENTAGE}")
    private MaxSizeConfig.MaxSizePolicy maxSizePolicy;

    /**
     * Eviction policy.
     */
    @Value("${hz.cluster.evition.policy:LRU}")
    private EvictionPolicy evictionPolicy;

    /**
     * Eviction percentage.
     */
    @Value("${hz.cluster.eviction.percentage:10}")
    private int evictionPercentage;

    /**
     * Max idle seconds.
     */
    @Value("${tgt.maxTimeToLiveInSeconds:28800}")
    private int maxIdleSeconds;

    public String getMapName() {
        return mapName;
    }

    public String getLoggingType() {
        return loggingType;
    }

    public String getMaxNoHeartbeatSeconds() {
        return maxNoHeartbeatSeconds;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public boolean isPortAutoIncrement() {
        return portAutoIncrement;
    }

    public int getPort() {
        return port;
    }

    public boolean isMulticastEnabled() {
        return multicastEnabled;
    }

    public boolean isTcpipEnabled() {
        return tcpipEnabled;
    }

    public List<String> getMembers() {
        return members;
    }

    public int getMaxHeapSizePercentage() {
        return maxHeapSizePercentage;
    }

    public MaxSizeConfig.MaxSizePolicy getMaxSizePolicy() {
        return maxSizePolicy;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public int getEvictionPercentage() {
        return evictionPercentage;
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }
}
