package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link HazelcastClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
public class HazelcastClusterProperties implements Serializable {
    private static final long serialVersionUID = 1817784607045775145L;
    /**
     * With PartitionGroupConfig, you can control how primary and backup partitions are mapped to physical Members.
     * Hazelcast will always place partitions on different partition groups so as to provide redundancy.
     * Accepted value are: {@code PER_MEMBER, HOST_AWARE, CUSTOM, ZONE_AWARE, SPI}.
     * In all cases a partition will never be created on the same group. If there are more partitions defined than
     * there are partition groups, then only those partitions, up to the number of partition groups, will be created.
     * For example, if you define 2 backups, then with the primary, that makes 3. If you have only two partition groups
     * only two will be created.
     * <ul>
     * <li>{@code}PER_MEMBER Partition Groups}: This is the default partition scheme and is used if no other scheme is defined.
     * Each Member is in a group of its own.</li>
     * <li>{@code}HOST_AWARE Partition Groups}: In this scheme, a group corresponds to a host, based on its IP address.
     * Partitions will not be written to any other members on the same host. This scheme provides good redundancy when multiple
     * instances are being run on the same host.</li>
     * <li>{@code}CUSTOM Partition Groups}: In this scheme, IP addresses, or IP address ranges, are allocated to groups.
     * Partitions are not written to the same
     * group. This is very useful for ensuring partitions are written to different racks or even availability zones.</li>
     * <li>{@code}ZONE_AWARE Partition Groups}:  In this scheme, groups are allocated according to the metadata provided
     * by Discovery SPI Partitions are not written to the same group. This is very useful for ensuring partitions are written to availability
     * zones or different racks without providing the IP addresses to the config ahead.</li>
     * <li>{@code}SPI Partition Groups}:  In this scheme, groups are allocated
     * according to the implementation provided by Discovery SPI.</li>
     * </ul>
     */
    private String partitionMemberGroupType;
    /**
     * Hazelcast has a flexible logging configuration and doesn't depend on any logging framework except JDK logging.
     * It has in-built adaptors for a number of logging frameworks and also supports custom loggers by providing logging interfaces.
     * To use built-in adaptors you should set this setting to one of predefined types below.
     * <ul>
     * <li>jdk: JDK logging</li>
     * <li>log4j: Log4j</li>
     * <li>slf4j: Slf4j</li>
     * <li>none: Disable logging</li>
     * </ul>
     */
    private String loggingType = "slf4j";
    /**
     * Max timeout of heartbeat in seconds for a node to assume it is dead.
     */
    private int maxNoHeartbeatSeconds = 300;
    /**
     * The instance name.
     */
    @RequiredProperty
    private String instanceName = "localhost";
    /**
     * You may also want to choose to use only one port. In that case, you can disable the auto-increment feature of port.
     */
    private boolean portAutoIncrement = true;
    /**
     * You can specify the ports which Hazelcast will use to communicate between cluster members.
     * The name of the parameter for this is port and its default value is 5701.
     * By default, Hazelcast will try 100 ports to bind. Meaning that, if you set the value of port as 5701,
     * as members are joining to the cluster, Hazelcast tries to find ports between 5701 and 5801.
     */
    @RequiredProperty
    private int port = 5701;
    /**
     * Enables a multicast configuration using a group address and port.
     * Contains the configuration for the multicast discovery mechanism.
     * With the multicast discovery mechanism Hazelcast allows Hazelcast members to find each other using multicast.
     * So Hazelcast members do not need to know concrete addresses of members, they just multicast to everyone listening.
     * It depends on your environment if multicast is possible or allowed; otherwise you need to have a look at the tcp/ip cluster
     */
    private boolean multicastEnabled;
    /**
     * Enable TCP/IP config.
     * Contains the configuration for the Tcp/Ip join mechanism.
     * The Tcp/Ip join mechanism relies on one or more well known members. So when a new member wants to join a cluster, it will try to connect
     * to one of the well known members. If it is able to connect, it will now about all members in the cluster
     * and doesn't rely on these well known members anymore.
     */
    private boolean tcpipEnabled = true;
    /**
     * Sets the well known members.
     * If members is empty, calling this method will have the same effect as calling clear().
     * A member can be a comma separated string, e..g '10.11.12.1,10.11.12.2' which indicates multiple members are going to be added.
     */
    @RequiredProperty
    private List<String> members = CollectionUtils.wrap("localhost");
    /**
     * Sets the maximum size of the map.
     */
    private int maxHeapSizePercentage = 85;
    /**
     * <ul>
     * <li>FREE_HEAP_PERCENTAGE: Policy based on minimum free JVM heap memory percentage per JVM.</li>
     * <li>FREE_HEAP_SIZE: Policy based on minimum free JVM heap memory in megabytes per JVM.</li>
     * <li>FREE_NATIVE_MEMORY_PERCENTAGE: Policy based on minimum free native memory percentage per Hazelcast instance.</li>
     * <li>FREE_NATIVE_MEMORY_SIZE: Policy based on minimum free native memory in megabytes per Hazelcast instance.</li>
     * <li>PER_NODE: Policy based on maximum number of entries stored per data structure (map, cache etc) on each Hazelcast instance.</li>
     * <li>PER_PARTITION: Policy based on maximum number of entries stored per data structure (map, cache etc) on each partition.</li>
     * <li>USED_HEAP_PERCENTAGE: Policy based on maximum used JVM heap memory percentage per data structure (map, cache etc) on each Hazelcast instance
     * .</li>
     * <li>USED_HEAP_SIZE: Policy based on maximum used JVM heap memory in megabytes per data structure (map, cache etc) on each Hazelcast instance.</li>
     * <li>USED_NATIVE_MEMORY_PERCENTAGE: Policy based on maximum used native memory percentage per data structure (map, cache etc) on each Hazelcast
     * instance.</li>
     * <li>USED_NATIVE_MEMORY_SIZE: Policy based on maximum used native memory in megabytes per data structure (map, cache etc) on each Hazelcast instance
     * .</li>
     * </ul>
     */
    private String maxSizePolicy = "USED_HEAP_PERCENTAGE";
    /**
     * Hazelcast supports policy-based eviction for distributed maps. Currently supported policies
     * are LRU (Least Recently Used) and LFU (Least Frequently Used) and NONE.
     * See <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Distributed_Data_Structures/Map/Map_Eviction.html">this</a> for more info.
     */
    private String evictionPolicy = "LRU";
    /**
     * To provide data safety, Hazelcast allows you to specify the number of backup copies you want to have. That way,
     * data on a cluster member will be copied onto other member(s).
     * To create synchronous backups, select the number of backup copies.
     * When this count is 1, a map entry will have its backup on one other member in the cluster. If you set it to 2, then a map entry will
     * have its backup on two other members. You can set it to 0
     * if you do not want your entries to be backed up, e.g., if performance is more important than backing up.
     * The maximum value for the backup count is 6.
     * Sync backup operations have a blocking cost which may lead to latency issues.
     */
    private int backupCount = 1;
    /**
     * Hazelcast supports both synchronous and asynchronous backups. By default, backup operations are synchronous.
     * In this case, backup operations block operations until backups are successfully copied to backup members
     * (or deleted from backup members in case of remove) and acknowledgements are received. Therefore, backups are updated before a put operation
     * is completed, provided that the cluster is stable.
     * Asynchronous backups, on the other hand, do not block operations. They are
     * fire and forget and do not require acknowledgements; the backup operations are performed at some point in time.
     */
    private int asyncBackupCount;

    /**
     * Connection timeout in seconds for the TCP/IP config
     * and members joining the cluster.
     */
    private int timeout = 5;

    /**
     * IPv6 support has been switched off by default, since some platforms
     * have issues in use of IPv6 stack. And some other platforms such as Amazon AWS have no support at all. To enable IPv6 support
     * set this setting to false.
     */
    private boolean ipv4Enabled = true;

    /**
     * Multicast trusted interfaces for discovery.
     * With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find each other using multicast communication.
     * The cluster members do not need to know the concrete addresses of the other members,
     * as they just multicast to all the other members for listening. Whether multicast is possible or allowed depends on your environment.
     */
    private String multicastTrustedInterfaces;
    /**
     * The multicast group address used for discovery.
     * With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find each other using multicast communication.
     * The cluster members do not need to know the concrete addresses of the other members,
     * as they just multicast to all the other members for listening. Whether multicast is possible or allowed depends on your environment.
     */
    private String multicastGroup;
    /**
     * The multicast port used for discovery.
     */
    private int multicastPort;
    /**
     * specifies the time in seconds that a member should wait for a valid multicast response from another
     * member running in the network before declaring itself the leader member (the first member joined to the cluster)
     * and creating its own cluster. This only applies to the startup of members where no leader has been assigned yet.
     * If you specify a high value, such as 60 seconds, it means that until a leader is selected,
     * each member will wait 60 seconds before moving on.
     * Be careful when providing a high value. Also, be careful not to set the value too low,
     * or the members might give up too early and create their own cluster.
     */
    private int multicastTimeout = 2;
    /**
     * Gets the time to live for the multicast package in seconds.
     * This is the default time-to-live for multicast packets sent out on the socket
     */
    private int multicastTimeToLive = 32;

    /**
     * Describe discovery strategies for Hazelcast.
     */
    @NestedConfigurationProperty
    private HazelcastDiscoveryProperties discovery = new HazelcastDiscoveryProperties();
    
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

    public HazelcastDiscoveryProperties getDiscovery() {
        return discovery;
    }

    public void setDiscovery(final HazelcastDiscoveryProperties discovery) {
        this.discovery = discovery;
    }

    public String getPartitionMemberGroupType() {
        return partitionMemberGroupType;
    }

    public void setPartitionMemberGroupType(final String partitionMemberGroupType) {
        this.partitionMemberGroupType = partitionMemberGroupType;
    }
}
