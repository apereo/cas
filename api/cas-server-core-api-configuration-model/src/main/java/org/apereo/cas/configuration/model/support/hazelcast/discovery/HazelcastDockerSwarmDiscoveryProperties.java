package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastDockerSwarmDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-discovery-swarm")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastDockerSwarmDiscoveryProperties implements Serializable {
    private static final long serialVersionUID = -1409066358752067150L;

    /**
     * Swarm DNSRR network binding.
     */
    private DnsRProvider dnsProvider = new DnsRProvider();

    /**
     * Local network binding.
     */
    private MemberAddressProvider memberProvider = new MemberAddressProvider();

    @RequiresModule(name = "cas-server-support-hazelcast-discovery-swarm")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class DnsRProvider implements Serializable {
        private static final long serialVersionUID = -1863901001243353934L;
        /**
         * Enable provider.
         */
        private boolean enabled;

        /**
         * Name of the docker service that this instance is running in.
         */
        private String serviceName;

        /**
         * Internal port that hazelcast is listening on.
         */
        private int servicePort = 5701;

        /**
         * Comma separated list of docker services and associated ports
         * to be considered peers of this service.
         * Note, this must include itself (the definition of
         * serviceName and servicePort) if the service is to
         * cluster with other instances of this service.
         */
        private String peerServices;
    }

    @RequiresModule(name = "cas-server-support-hazelcast-discovery-swarm")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MemberAddressProvider implements Serializable {
        private static final long serialVersionUID = -2963901001243353939L;

        /**
         * Enable provider.
         */
        private boolean enabled;

        /**
         * Comma delimited list of Docker network names to discover matching services on.
         */
        private String dockerNetworkNames;

        /**
         * Comma delimited list of relevant Docker service names to find tasks/containers on the networks.
         */
        private String dockerServiceNames;

        /**
         * Comma delimited list of relevant Docker service label=values to find tasks/containers on the networks.
         */
        private String dockerServiceLabels;

        /**
         * Swarm Manager URI (overrides DOCKER_HOST).
         */
        private String swarmMgrUri;

        /**
         * If Swarm Mgr URI is SSL, to enable skip-verify for it.
         */
        private boolean skipVerifySsl;

        /**
         * The raw port that hazelcast is listening on.
         * IMPORTANT: This is NOT a docker "published" port, nor is it necessarily
         * a EXPOSEd port. It is simply the hazelcast port that the service
         * is configured with, this must be the same for all matched containers
         * in order to work, and just using the default of 5701 is the simplest
         * way to go.
         */
        private int hazelcastPeerPort = 5701;
    }
}
