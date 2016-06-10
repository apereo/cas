package org.apereo.cas.configuration.model.support.hazelcast;

import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates hazelcast properties exposed by CAS via properties file property source in a type-safe manner.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */

public class HazelcastProperties {

    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";

    /**
     * Max num of seconds for heartbeat property name.
     */
    public static final String MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds";

    private String mapName = "tickets";
    private int pageSize = 500;
    private Resource configLocation;
    
    private Cluster cluster = new Cluster();

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(final Cluster cluster) {
        this.cluster = cluster;
    }

    public static class Cluster {
        private String loggingType = "slf4j";
        private int maxNoHeartbeatSeconds = 300;
        private String instanceName = "localhost";
        private boolean portAutoIncrement = true;
        private int port = 5701;
        private boolean multicastEnabled;
        private boolean tcpipEnabled = true;
        private List<String> members = Arrays.asList("localhost");
        private int maxHeapSizePercentage = 85;
        private String maxSizePolicy;
        private String evictionPolicy;
        private int evictionPercentage;

        public String getLoggingType() {
            return loggingType;
        }

        public void setLoggingType(final String loggingType) {
            this.loggingType = loggingType;
        }

        public int getMaxNoHeartbeatSeconds() {
            return maxNoHeartbeatSeconds;
        }

        public void setMaxNoHeartbeatSeconds(final int maxNoHeartbeatSeconds) {
            this.maxNoHeartbeatSeconds = maxNoHeartbeatSeconds;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(final String instanceName) {
            this.instanceName = instanceName;
        }

        public boolean isPortAutoIncrement() {
            return portAutoIncrement;
        }

        public void setPortAutoIncrement(final boolean portAutoIncrement) {
            this.portAutoIncrement = portAutoIncrement;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public boolean isMulticastEnabled() {
            return multicastEnabled;
        }

        public void setMulticastEnabled(final boolean multicastEnabled) {
            this.multicastEnabled = multicastEnabled;
        }

        public boolean isTcpipEnabled() {
            return tcpipEnabled;
        }

        public void setTcpipEnabled(final boolean tcpipEnabled) {
            this.tcpipEnabled = tcpipEnabled;
        }

        public List<String> getMembers() {
            return members;
        }

        public void setMembers(final List<String> members) {
            this.members = members;
        }

        public int getMaxHeapSizePercentage() {
            return maxHeapSizePercentage;
        }

        public void setMaxHeapSizePercentage(final int maxHeapSizePercentage) {
            this.maxHeapSizePercentage = maxHeapSizePercentage;
        }

        public String getMaxSizePolicy() {
            return maxSizePolicy;
        }

        public void setMaxSizePolicy(final String maxSizePolicy) {
            this.maxSizePolicy = maxSizePolicy;
        }

        public String getEvictionPolicy() {
            return evictionPolicy;
        }

        public void setEvictionPolicy(final String evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
        }

        public int getEvictionPercentage() {
            return evictionPercentage;
        }

        public void setEvictionPercentage(final int evictionPercentage) {
            this.evictionPercentage = evictionPercentage;
        }
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(final String mapName) {
        this.mapName = mapName;
    }
}
