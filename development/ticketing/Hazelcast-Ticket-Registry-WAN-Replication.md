# Hazelcast Ticket Registry - WAN Replication

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-WAN-Replication.html

Hazelcast WAN Replication allows you to keep multiple Hazelcast clusters in sync by replicating their state over WAN environments such as the Internet.

> **Usage Warning!**
>
> Using Hazelcast WAN Replication requires a Hazelcast Enterprise subscription. Make sure you have acquired the proper license, SDK and tooling from Hazelcast before activating this feature. Please contact Hazelcast for more information.

Hazelcast supports two different operation modes of WAN Replication:

- Active-Passive: This mode is mostly used for failover scenarios where you want to replicate an active cluster to one or more passive clusters, for the purpose of maintaining a backup.
- Active-Active: Every cluster is equal, each cluster replicates to all other clusters. This is normally used to connect different clients to different clusters for the sake of the shortest path between client and server.

Defining WAN replication endpoints in CAS is done using static endpoints and discovery.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.wan-replication.enabled` (Optional; type `Boolean`; default `false`): Whether WAN should be enabled.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.replication-name` (Optional; type `String`; default `apereo-cas`): Name of this replication group.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets` (Optional; type `List<HazelcastWANReplicationTargetClusterProperties>`): List of target clusters to be used for synchronization and replication.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].acknowledge-type` (Optional; type `String`; default `ACK_ON_OPERATION_COMPLETE`): Accepted acknowledgement-type values are listed below. - `ACK_ON_RECEIPT`: ACK after WAN operation is received by the target cluster (without waiting the result of actual operation invocation). - `ACK_ON_OPERATION_COMPLETE`: Wait till the operation is complete on target cluster.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].batch-maximum-delay-milliseconds` (Optional; type `int`; default `1000`): Maximum amount of time, in milliseconds, to be waited before sending a batch of events in case batch.size is not reached.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].batch-size` (Optional; type `int`; default `500`): Maximum size of events that are sent to the target cluster in a single batch.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].cluster-name` (Optional; type `String`): Sets the cluster name used as an endpoint group password for authentication on the target endpoint. If there is no separate publisher ID property defined, this cluster name will also be used as a WAN publisher ID. This ID is then used for identifying the publisher.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].consistency-check-strategy` (Optional; type `String`; default `NONE`): Strategy for checking the consistency of data between replicas.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].endpoints` (Optional; type `String`): Comma separated list of endpoints in this replication group. IP addresses and ports of the cluster members for which the WAN replication is implemented. These endpoints are not necessarily the entire target cluster and WAN does not perform the discovery of other members in the target cluster. It only expects that these IP addresses (or at least some of them) are available.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].executor-thread-count` (Optional; type `int`; default `2`): The number of threads that the replication executor will have. The executor is used to send WAN events to the endpoints and ideally you want to have one thread per endpoint. If this property is omitted and you have specified the endpoints property, this will be the case. If necessary you can manually define the number of threads that the executor will use. Once the executor has been initialized there is thread affinity between the discovered endpoints and the executor threads - all events for a single endpoint will go through a single executor thread, preserving event order. It is important to determine which number of executor threads is a good value. Failure to do so can lead to performance issues - either contention on a too small number of threads or wasted threads that will not be performing any work.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].properties` (Optional; type `Map<String, Comparable>`): The WAN publisher properties.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].publisher-class-name` (Optional; type `String`; default `com.hazelcast.enterprise.wan.replication.WanBatchReplication`): Publisher class name for WAN replication.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].publisher-id` (Optional; type `String`): Returns the publisher ID used for identifying the publisher.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].queue-capacity` (Optional; type `int`; default `10000`): For huge clusters or high data mutation rates, you might need to increase the replication queue size. The default queue size for replication queues is 10,000. This means, if you have heavy put/update/remove rates, you might exceed the queue size so that the oldest, not yet replicated, updates might get lost.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].queue-full-behavior` (Optional; type `String`; default `THROW_EXCEPTION`): Accepted queue-full behavior values are listed below. - `THROW_EXCEPTION`: Instruct WAN replication implementation to throw an exception and doesn't allow further processing. - `DISCARD_AFTER_MUTATION`: Instruct WAN replication implementation to drop new events when WAN event queues are full. - `THROW_EXCEPTION_ONLY_IF_REPLICATION_ACTIVE`: Similar to `THROW_EXCEPTION` but only throws exception when WAN replication is active. * Discards the new events if WAN replication is stopped.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].response-timeout-milliseconds` (Optional; type `int`; default `60000`): Time, in milliseconds, to be waited for the acknowledgment of a sent WAN event to target cluster.
- `cas.ticket.registry.hazelcast.cluster.wan-replication.targets[0].snapshot-enabled` (Optional; type `boolean`): When set to true, only the latest events (based on key) are selected and sent in a batch.
