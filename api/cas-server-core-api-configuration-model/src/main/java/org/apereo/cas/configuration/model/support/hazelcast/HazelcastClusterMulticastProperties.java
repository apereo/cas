package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastClusterMulticastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastClusterMulticastProperties")
public class HazelcastClusterMulticastProperties implements Serializable {

    private static final long serialVersionUID = 1827784607045775145L;

    /**
     * Multicast trusted interfaces for discovery.
     * With the multicast auto-discovery mechanism, Hazelcast allows cluster
     * members to find each other using multicast communication.
     * The cluster members do not need to know the concrete addresses of the other members,
     * as they just multicast to all the other members for listening. Whether
     * multicast is possible or allowed depends on your environment.
     */
    private String trustedInterfaces;

    /**
     * The multicast group address used for discovery.
     * With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find each other using multicast communication.
     * The cluster members do not need to know the concrete addresses of the other members,
     * as they just multicast to all the other members for listening. Whether multicast is possible or allowed depends on your environment.
     */
    private String group;

    /**
     * The multicast port used for discovery.
     */
    private int port;

    /**
     * specifies the time in seconds that a member should wait for a valid multicast response from another
     * member running in the network before declaring itself the leader member (the first member joined to the cluster)
     * and creating its own cluster. This only applies to the startup of members where no leader has been assigned yet.
     * If you specify a high value, such as 60 seconds, it means that until a leader is selected,
     * each member will wait 60 seconds before moving on.
     * Be careful when providing a high value. Also, be careful not to set the value too low,
     * or the members might give up too early and create their own cluster.
     */
    private int timeout = 2;

    /**
     * Gets the time to live for the multicast package in seconds.
     * This is the default time-to-live for multicast packets sent out on the socket
     */
    private int timeToLive = 32;

    /**
     * Enables a multicast configuration using a group address and port.
     * Contains the configuration for the multicast discovery mechanism.
     * With the multicast discovery mechanism Hazelcast allows Hazelcast members to find each other using multicast.
     * So Hazelcast members do not need to know concrete addresses of members, they just multicast to everyone listening.
     * It depends on your environment if multicast is possible or allowed; otherwise you need to have a look at the tcp/ip cluster
     */
    private boolean enabled;
}
