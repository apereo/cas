package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastCoreClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastCoreClusterProperties")
public class HazelcastCoreClusterProperties implements Serializable {
    private static final long serialVersionUID = -8374968308106013185L;

    /**
     * Used when replication is turned on with {@link #isReplicated()}.
     *
     * <p>
     * If a new member joins the cluster, there are two ways you can handle the initial
     * provisioning that is executed to replicate all existing values to the new member.
     * Each involves how you configure the async fill up.
     * <ul>
     * <li>First, you can configure async fill up to true, which does not block reads while
     * the fill up operation is underway. That way, you have immediate access on the
     * new member, but it will take time until all the values are eventually accessible.
     * Not yet replicated values are returned as non-existing (null). </li>
     * <li>Second, you can configure for a synchronous initial fill up (by configuring the
     * async fill up to false), which blocks every read or write access to the map
     * until the fill up operation is finished. Use this with caution since
     * it might block your application from operating.</li>
     * </ul>
     */
    private boolean asyncFillup = true;

    /**
     * A Replicated Map is a distributed key-value data structure where the data is replicated
     * to all members in the cluster. It provides full replication of
     * entries to all members for high speed access. A Replicated Map does not partition data
     * (it does not spread data to different cluster members); instead, it replicates the data to all members.
     * Replication leads to higher memory consumption. However, a Replicated Map has faster
     * read and write access since the data is available on all members.
     * Writes could take place on local/remote members in order to provide
     * write-order, eventually being replicated to all other members.
     *
     * <br><p>
     * If you have a large cluster or very high occurrences of updates, the Replicated Map may not
     * scale linearly as expected since it has to replicate update operations to all members in the cluster.
     * Since the replication of updates is performed in an asynchronous manner, Hazelcast recommends you
     * enable back pressure in case your system has high occurrences of updates.
     *
     * Note that Replicated Map does not guarantee eventual consistency because there are
     * some edge cases that fail to provide consistency.
     *
     * <br><p>
     * Replicated Map uses the internal partition system of Hazelcast in order to
     * serialize updates happening on the same key at the same time. This happens by
     * sending updates of the same key to the same Hazelcast member in the cluster.
     *
     * <br><p>
     * Due to the asynchronous nature of replication, a Hazelcast member could die before successfully
     * replicating a "write" operation to other members after sending the "write completed"
     * response to its caller during the write process. In this scenario, Hazelcast’s internal
     * partition system promotes one of the replicas of the partition as the primary one.
     * The new primary partition does not have the latest "write" since the
     * dead member could not successfully replicate the update.
     */
    private boolean replicated;

    /**
     * With {@code PartitionGroupConfig}, you can control how primary and backup partitions are mapped to physical Members.
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
     * <li>{@code}ZONE_AWARE Partition Groups}: In this scheme, groups are allocated according to the metadata provided
     * by Discovery SPI Partitions are not written to the same group. This is very useful for ensuring partitions are written to availability
     * zones or different racks without providing the IP addresses to the config ahead.</li>
     * <li>{@code}SPI Partition Groups}:  In this scheme, groups are allocated
     * according to the implementation provided by Discovery SPI.</li>
     * </ul>
     */
    private String partitionMemberGroupType;

    /**
     * Hazelcast has a flexible logging configuration and doesn't depend on any logging framework except JDK logging.
     * It has in-built adaptors for a number of logging frameworks and
     * also supports custom loggers by providing logging interfaces.
     * To use built-in adaptors you should set this setting to one of predefined types below.
     * <ul>
     * <li>{@code jdk}: JDK logging</li>
     * <li>{@code log4j}: Log4j</li>
     * <li>{@code slf4j}: Slf4j</li>
     * <li>{@code none}: Disable logging</li>
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
    private String instanceName;

    /**
     * Define how data items in Hazelcast maps are merged together from source to destination.
     * By default, merges map entries from source to destination if they don't exist in the destination map.
     * Accepted values are:
     * <ul>
     *     <li>{@code PUT_IF_ABSENT}: Merges data structure entries from source to destination if they don't exist in the destination data structure.</li>
     *     <li>{@code HIGHER_HITS}:  * Merges data structure entries from source to destination data structure if the source entry
     *     has more hits than the destination one.</li>
     *     <li>{@code DISCARD}: Merges only entries from the destination data structure and discards all entries from the source data structure. </li>
     *     <li>{@code PASS_THROUGH}: Merges data structure entries from source to destination directly unless the merging entry is null</li>
     *     <li>{@code EXPIRATION_TIME}: Merges data structure entries from source to destination data structure if the source entry
     *     will expire later than the destination entry. This policy can only be used if the clocks of the nodes are in sync. </li>
     *     <li>{@code LATEST_UPDATE}:  Merges data structure entries from source to destination data structure if the source entry was
     *     updated more frequently than the destination entry. This policy can only be used if the clocks of the nodes are in sync. </li>
     *     <li>{@code LATEST_ACCESS}: Merges data structure entries from source to destination data structure if the source entry
     *     has been accessed more recently than the destination entry. This policy can only be used if the clocks of the nodes are in sync.</li>
     * </ul>
     */
    private String mapMergePolicy = "PUT_IF_ABSENT";

    /**
     * Sets the maximum size of the map.
     */
    private int maxSize = 85;

    /**
     * <ul>
     * <li>{@code FREE_HEAP_PERCENTAGE}: Policy based on minimum free JVM heap memory percentage per JVM.</li>
     * <li>{@code FREE_HEAP_SIZE}: Policy based on minimum free JVM heap memory in megabytes per JVM.</li>
     * <li>{@code FREE_NATIVE_MEMORY_PERCENTAGE}: Policy based on minimum free native memory percentage per Hazelcast instance.</li>
     * <li>{@code FREE_NATIVE_MEMORY_SIZE}: Policy based on minimum free native memory in megabytes per Hazelcast instance.</li>
     * <li>{@code PER_NODE}: Policy based on maximum number of entries stored per data structure (map, cache etc) on each Hazelcast instance.</li>
     * <li>{@code PER_PARTITION}: Policy based on maximum number of entries stored per data structure (map, cache etc) on each partition.</li>
     * <li>{@code USED_HEAP_PERCENTAGE}: Policy based on maximum used JVM heap memory percentage per data structure (map, cache etc) on each Hazelcast instance
     * .</li>
     * <li>{@code USED_HEAP_SIZE}: Policy based on maximum used JVM heap memory in megabytes per data structure (map, cache etc) on each Hazelcast instance.</li>
     * <li>{@code USED_NATIVE_MEMORY_PERCENTAGE}: Policy based on maximum used native memory percentage per data structure (map, cache etc) on each Hazelcast
     * instance.</li>
     * <li>{@code USED_NATIVE_MEMORY_SIZE}: Policy based on maximum used native memory in megabytes per data structure (map, cache etc) on each Hazelcast instance
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
     * (or deleted from backup members in case of remove) and acknowledgements are received.
     * Therefore, backups are updated before a put operation
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
     * CP Subsystem is a component of a Hazelcast cluster that builds a strongly
     * consistent layer for a set of distributed data structures.
     * Its data structures are CP with respect to the CAP
     * principle, i.e., they always maintain linearizability
     * and prefer consistency over availability during network partitions. Besides network
     * partitions, CP Subsystem withstands server and client failures.
     * All members of a Hazelcast cluster do not necessarily take part in CP Subsystem. The number of Hazelcast
     * members that take part in CP Subsystem is specified here.
     * CP Subsystem must have at least 3 CP members.
     */
    private int cpMemberCount;

}
