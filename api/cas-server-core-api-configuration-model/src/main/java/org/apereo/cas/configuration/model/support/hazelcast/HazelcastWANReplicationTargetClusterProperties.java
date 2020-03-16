package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link HazelcastWANReplicationTargetClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastWANReplicationTargetClusterProperties implements Serializable {
    private static final long serialVersionUID = 1635330607045885145L;

    /**
     * Comma separated list of endpoints in this replication group.
     * IP addresses and ports of the cluster members for which the WAN replication is implemented. These endpoints are not necessarily
     * the entire target cluster and WAN does not perform the discovery of other members in the target cluster. It only expects
     * that these IP addresses (or at least some of them) are available.
     */
    private String endpoints;

    /**
     * Sets the cluster name used as an endpoint group password for authentication
     * on the target endpoint.
     * If there is no separate publisher ID property defined, this cluster name
     * will also be used as a WAN publisher ID. This ID is then used for
     * identifying the publisher.
     */
    private String clusterName;

    /**
     * Returns the publisher ID used for identifying the publisher.
     */
    private String publisherId;

    /**
     * The WAN publisher properties.
     */
    private Map<String, Comparable> properties = new HashMap<>();

    /**
     * Strategy for checking the consistency of data between replicas.
     */
    private String consistencyCheckStrategy = "NONE";

    /**
     * Publisher class name for WAN replication.
     */
    private String publisherClassName = "com.hazelcast.enterprise.wan.replication.WanBatchReplication";

    /**
     * Accepted values are:
     * <ul>
     * <li>{@code THROW_EXCEPTION}: Instruct WAN replication implementation to throw an exception and doesn't allow further processing.</li>
     * <li>{@code DISCARD_AFTER_MUTATION}: Instruct WAN replication implementation to drop new events when WAN event queues are full.</li>
     * <li>{@code THROW_EXCEPTION_ONLY_IF_REPLICATION_ACTIVE}: Similar to {@code THROW_EXCEPTION} but only throws exception when WAN replication is active.
     * * Discards the new events if WAN replication is stopped.</li>
     * </ul>
     */
    private String queueFullBehavior = "THROW_EXCEPTION";

    /**
     * Accepted values are:
     * <ul>
     *     <li>{@code ACK_ON_RECEIPT}: ACK after WAN operation is received by the target cluster (without waiting the result of actual operation invocation).</li>
     *     <li>{@code ACK_ON_OPERATION_COMPLETE}: Wait till the operation is complete on target cluster.</li>
     * </ul>
     */
    private String acknowledgeType = "ACK_ON_OPERATION_COMPLETE";

    /**
     * For huge clusters or high data mutation rates, you might need to increase the replication queue size.
     * The default queue size for replication queues is 10,000. This means, if you have heavy put/update/remove
     * rates, you might exceed the queue size so that the oldest, not yet replicated, updates might get
     * lost.
     */
    private int queueCapacity = 10_000;

    /**
     * Maximum size of events that are sent to the target cluster in a single batch.
     */
    private int batchSize = 500;

    /**
     * When set to true, only the latest events (based on key) are selected and sent in a batch.
     */
    private boolean snapshotEnabled;

    /**
     * Maximum amount of time, in milliseconds, to be waited before
     * sending a batch of events in case batch.size is not reached.
     */
    private int batchMaximumDelayMilliseconds = 1000;

    /**
     * Time, in milliseconds, to be waited for the acknowledgment of a sent WAN event to target cluster.
     */
    private int responseTimeoutMilliseconds = 60_000;

    /**
     * The number of threads that the replication executor will have. The executor is used to send WAN
     * events to the endpoints and ideally you want to have one thread per endpoint. If this property is omitted
     * and you have specified the endpoints property, this will be the case. If necessary you can manually define
     * the number of threads that the executor will use. Once the executor has been initialized there is thread
     * affinity between the discovered endpoints and the executor threads - all events for a single endpoint will
     * go through a single executor thread, preserving event order. It is important to determine which number of
     * executor threads is a good value. Failure to do so can lead to performance issues - either contention on a
     * too small number of threads or wasted threads that will not be performing any work.
     */
    private int executorThreadCount = 2;
}
