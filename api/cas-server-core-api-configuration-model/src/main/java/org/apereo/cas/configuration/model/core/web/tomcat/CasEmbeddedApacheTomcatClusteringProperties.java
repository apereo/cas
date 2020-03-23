package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatClusteringProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
public class CasEmbeddedApacheTomcatClusteringProperties implements Serializable {
    private static final long serialVersionUID = 620356002948464740L;

    /**
     * Accepted values are: {@code DEFAULT, CLOUD}.
     * Type of clustering to use, set to {@code CLOUD} if using CloudMembershipService.
     */
    private String clusteringType = "DEFAULT";

    /**
     * Cloud membership provider, values are case sensitive and only used with clusteringType {@code CLOUD}.
     * The different providers rely on environment variables to discover other members of cluster
     * via DNS lookups of the service name or querying kubernetes API. See code or Tomcat documentation
     * for the environment variables that are used.
     * <ul>
     * <li> {@code kubernetes} will use org.apache.catalina.tribes.KubernetesMembershipProvider</li>
     * <li> {@code dns} will use org.apache.catalina.tribes.DNSMembershipProvider</li>
     * <li> class implementing {@code org.apache.catalina.tribes.MembershipProvider}</li>
     * </ul>
     */
    private String cloudMembershipProvider = "kubernetes";

    /**
     * When a web application is being shutdown, Tomcat issues an expire call to each session to notify all the listeners.
     * If you wish for all sessions to expire on all nodes when a shutdown occurs on one node, set this value to true. Default value is false.
     */
    private boolean expireSessionsOnShutdown;

    /**
     * This option is used to set the flag that all messages sent through the SimpleTcpCluster uses.
     * The flag decides how the messages are sent, and is a simple logical OR.
     * <ul>
     * <li>2: {@code SEND_OPTIONS_SYNCHRONIZED_ACK}</li>
     * <li>4: {@code SEND_OPTIONS_USE_ACK}</li>
     * <li>8: {@code SEND_OPTIONS_ASYNCHRONOUS}</li>
     * </ul>
     */
    private int channelSendOptions = 8;
    /**
     * The listen port for incoming data. The default value is 4000. To avoid port conflicts the receiver will automatically bind to a free port.
     * So for example, if port is 4000, and autoBind is set to 10, then the receiver will open up a server socket on the first available port in the range 4000-4009.
     */
    private int receiverPort = 4000;
    /**
     * Listener timeout.
     * The value in milliseconds for the polling timeout in the NioReceiver. On older versions of the JDK there have been
     * bugs, that should all now be cleared out where the selector never woke up. The default value is a very high 5000 milliseconds.
     */
    private int receiverTimeout = 5000;
    /**
     * Maximum threads configured for the listener.
     * The maximum number of threads in the receiver thread pool. The default value is 6 Adjust this value relative to
     * the number of nodes in the cluster, the number
     * of messages being exchanged and the hardware you are running on. A higher value doesn't mean
     * more efficiency, tune this value according to your own test results.
     */
    private int receiverMaxThreads = 6;
    /**
     * The address (network interface) to listen for incoming traffic.
     */
    private String receiverAddress = "auto";
    /**
     * Default value is 100. Use this value if you wish to automatically avoid port conflicts the
     * cluster receiver will try to open a server socket on the port attribute port, and then work up autoBind number of times.
     */
    private int receiverAutoBind = 100;

    /**
     * Statically register members in the cluster. The syntax is: {@code address:port:index}
     */
    private String clusterMembers;

    /**
     * Multicast port (the port and the address together determine cluster membership.
     * The multicast port, the default value is 45564
     * The multicast port, in conjunction with the address is what creates a cluster group. To divide up your farm
     * into several different group, or to split up QA from production, change the port or the address
     */
    private int membershipPort = 45564;
    /**
     * Multicast address for membership.
     * The multicast address that the membership will broadcast its presence and listen for other heartbeats on.
     * The default value is 228.0.0.4 Make sure your network is enabled for multicast traffic.
     * The multicast address, in conjunction with the port is what creates a cluster group. To divide up your farm into several
     * different group, or to split up QA from production, change the port or the address
     */
    private String membershipAddress = "228.0.0.4";
    /**
     * The frequency in milliseconds in which heartbeats are sent out. The default value is 500 ms.
     * In most cases the default value is sufficient. Changing this value, simply changes the interval in between heartbeats.
     */
    private int membershipFrequency = 500;
    /**
     * The membership component will time out members and notify the Channel if a member fails to send a heartbeat
     * within a give time. The default value is 3000 ms. This means, that if a heartbeat is not received from a member in that timeframe,
     * the membership component will notify the cluster of this.
     * On a high latency network you may wish to increase this value, to protect against false positives.
     * Apache Tribes also provides a TcpFailureDetector that will verify a timeout using a TCP connection when a
     * heartbeat timeout has occurred. This protects against false positives.
     */
    private int membershipDropTime = 3000;

    /**
     * In case of a network failure, Java multicast socket don't transparently fail over, instead the socket will continuously
     * throw IOException upon each receive request. When recoveryEnabled is set to true, this will close the multicast socket
     * and open a new socket with the same properties as defined above.
     * The default is true.
     */
    private boolean membershipRecoveryEnabled = true;

    /**
     * Membership uses multicast, it will call java.net.MulticastSocket.setLoopbackMode(localLoopbackDisabled).
     * When localLoopbackDisabled==true multicast messages will not reach other nodes on the same local machine. The default is false.
     */
    private boolean membershipLocalLoopbackDisabled;

    /**
     * Membership uses multicast, it will call java.net.MulticastSocket.setLoopbackMode(localLoopbackDisabled).
     * When localLoopbackDisabled==true multicast messages will not reach other nodes on the same local machine. The default is false.
     */
    private int membershipRecoveryCounter = 10;

    /**
     * Accepted values are: {@code DELTA, BACKUP}.
     * Enable all-to-all session replication using the DeltaManager to replicate session deltas.
     * By all-to-all we mean that the session gets replicated to all the other nodes in the cluster.
     * This works great for smaller cluster but we don't recommend it for larger clusters(a lot of Tomcat nodes).
     * Also when using the delta manager it will replicate to all nodes, even nodes that don't have the application deployed.
     * To get around this problem, you'll want to use the BackupManager. This manager only replicates the session data
     * to one backup node, and only to nodes that have the application deployed.
     * Downside of the BackupManager: not quite as battle tested as the delta manager.
     */
    private String managerType = "DELTA";

    /**
     * Enable tomcat session clustering.
     */
    private boolean enabled;
}
