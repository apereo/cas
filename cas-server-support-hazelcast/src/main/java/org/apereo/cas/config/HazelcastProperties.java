package org.apereo.cas.config;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RefreshScope
@Component("hazelcastProperties")
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
        return this.mapName;
    }

    public String getLoggingType() {
        return this.loggingType;
    }

    public String getMaxNoHeartbeatSeconds() {
        return this.maxNoHeartbeatSeconds;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public boolean isPortAutoIncrement() {
        return this.portAutoIncrement;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isMulticastEnabled() {
        return this.multicastEnabled;
    }

    public boolean isTcpipEnabled() {
        return this.tcpipEnabled;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public int getMaxHeapSizePercentage() {
        return this.maxHeapSizePercentage;
    }

    public MaxSizeConfig.MaxSizePolicy getMaxSizePolicy() {
        return this.maxSizePolicy;
    }

    public EvictionPolicy getEvictionPolicy() {
        return this.evictionPolicy;
    }

    public int getEvictionPercentage() {
        return this.evictionPercentage;
    }

    public int getMaxIdleSeconds() {
        return this.maxIdleSeconds;
    }
}
