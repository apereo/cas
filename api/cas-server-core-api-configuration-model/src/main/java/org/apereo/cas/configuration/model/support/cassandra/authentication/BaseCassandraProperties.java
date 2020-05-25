package org.apereo.cas.configuration.model.support.cassandra.authentication;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link BaseCassandraProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-cassandra-core")
@Accessors(chain = true)
public abstract class BaseCassandraProperties implements Serializable {

    private static final long serialVersionUID = 3708645268337674572L;

    /**
     * Username to bind and establish a connection to cassandra.
     */
    @RequiredProperty
    private String username;

    /**
     * Password to bind and establish a connection to cassandra.
     */
    @RequiredProperty
    private String password;

    /**
     * Keyspace address to use where the cluster would connect.
     */
    @RequiredProperty
    private String keyspace;

    /**
     * The list of contact points to use for the new cluster.
     */
    @RequiredProperty
    private List<String> contactPoints = Stream.of("localhost:9042").collect(Collectors.toList());

    /**
     * Used by a DC-ware round-robin load balancing policy.
     * This policy provides round-robin queries over the node of the local data center. It also includes in the query plans returned a
     * configurable number of hosts in the remote data centers, but those are always tried after the local nodes.
     * In other words, this policy guarantees that no host in a remote data center will be queried unless no host in the local data center can be reached.
     */
    private String localDc;

    /**
     * Query option consistency level.
     * The consistency level set through this method will be use for queries that don't explicitly have a consistency level.
     * Accepted values are:{@code ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO}.
     */
    private String consistencyLevel = "LOCAL_QUORUM";

    /**
     * The request timeout.
     * This defines how long the driver will wait for a given Cassandra node to answer a query.
     */
    private String timeout = "PT5S";

    /**
     * Query option serial consistency level.
     * The serial consistency level set through this method will be use for queries that don't explicitly have a serial consistency level.
     * Accepted values are:{@code ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO}.
     */
    private String serialConsistencyLevel = "LOCAL_SERIAL";
}
