package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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

    private int pageSize = 500;
    private Resource configLocation;

    private Cluster cluster = new Cluster();

    @NestedConfigurationProperty
    private CryptographyProperties crypto = new CryptographyProperties();

    public CryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final CryptographyProperties crypto) {
        this.crypto = crypto;
    }

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
        private String maxSizePolicy = "USED_HEAP_PERCENTAGE";
        private String evictionPolicy = "LRU";
        private int backupCount = 1;
        private int asyncBackupCount;

        private int timeout = 5;

        private boolean ipv4Enabled = true;

        private String multicastTrustedInterfaces;
        private String multicastGroup;
        private int multicastPort;
        private int multicastTimeout = 2;
        private int multicastTimeToLive = 32;

        public int getBackupCount() {
            return backupCount;
        }

        public void setBackupCount(final int backupCount) {
            this.backupCount = backupCount;
        }

        public int getAsyncBackupCount() {
            return asyncBackupCount;
        }

        public void setAsyncBackupCount(final int asyncBackupCount) {
            this.asyncBackupCount = asyncBackupCount;
        }

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

        public String getMulticastTrustedInterfaces() {
            return multicastTrustedInterfaces;
        }

        public void setMulticastTrustedInterfaces(final String multicastTrustedInterfaces) {
            this.multicastTrustedInterfaces = multicastTrustedInterfaces;
        }

        public String getMulticastGroup() {
            return multicastGroup;
        }

        public void setMulticastGroup(final String multicastGroup) {
            this.multicastGroup = multicastGroup;
        }

        public int getMulticastPort() {
            return multicastPort;
        }

        public void setMulticastPort(final int multicastPort) {
            this.multicastPort = multicastPort;
        }

        public int getMulticastTimeout() {
            return multicastTimeout;
        }

        public void setMulticastTimeout(final int multicastTimeout) {
            this.multicastTimeout = multicastTimeout;
        }

        public int getMulticastTimeToLive() {
            return multicastTimeToLive;
        }

        public void setMulticastTimeToLive(final int multicastTimeToLive) {
            this.multicastTimeToLive = multicastTimeToLive;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(final int timeout) {
            this.timeout = timeout;
        }

        public boolean isIpv4Enabled() {
            return ipv4Enabled;
        }

        public void setIpv4Enabled(final boolean ipv4Enabled) {
            this.ipv4Enabled = ipv4Enabled;
        }
    }
}
