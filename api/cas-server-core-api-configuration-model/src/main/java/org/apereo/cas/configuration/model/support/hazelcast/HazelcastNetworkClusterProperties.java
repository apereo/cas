package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link HazelcastNetworkClusterProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastClusterProperties")
public class HazelcastNetworkClusterProperties implements Serializable {
    private static final long serialVersionUID = -8474968308106013185L;

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
     * A member can be a comma separated string, e..g '10.11.12.1,10.11.12.2' which
     * indicates multiple members are going to be added.
     */
    @RequiredProperty
    private List<String> members = Stream.of("localhost").collect(Collectors.toList());

    /**
     * You may also want to choose to use only one port. In that case,
     * you can disable the auto-increment feature of port.
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
     * The outbound ports for the Hazelcast configuration.
     */
    private List<String> outboundPorts = new ArrayList<>();

    /**
     * If this property is set, then this is the address where the server socket is bound to.
     */
    private String localAddress;

    /**
     * The default public address to be advertised to other cluster members and clients.
     */
    private String publicAddress;

    /**
     * You can specify which network interfaces that Hazelcast should use.
     * Servers mostly have more than one network interface, so you may want to
     * list the valid IPs. Range characters ('*' and '-') can be used for simplicity.
     * For instance, 10.3.10.* refers to IPs between 10.3.10.0 and 10.3.10.255.
     * Interface 10.3.10.4-18 refers to IPs between 10.3.10.4 and
     * 10.3.10.18 (4 and 18 included). If network interface configuration
     * is enabled (it is disabled by default) and if Hazelcast cannot find
     * an matching interface, then it will print a message on
     * the console and will not start on that node.
     * <p>
     * Interfaces can be separated by a comma.
     */
    private String networkInterfaces;

    /**
     * IPv6 support has been switched off by default, since some platforms
     * have issues in use of IPv6 stack. And some other platforms such as Amazon AWS have no support at all. To enable IPv6 support
     * set this setting to false.
     */
    private boolean ipv4Enabled = true;

    /**
     * You can use the SSL (Secure Sockets Layer) protocol to
     * establish an encrypted communication across your Hazelcast
     * cluster with key stores and trust stores.
     */
    @NestedConfigurationProperty
    private HazelcastNetworkSslProperties ssl = new HazelcastNetworkSslProperties();
}
