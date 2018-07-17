package org.apereo.cas.config;

import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatClusteringProperties;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.ha.session.BackupManager;
import org.apache.catalina.ha.session.ClusterManagerBase;
import org.apache.catalina.ha.session.ClusterSessionListener;
import org.apache.catalina.ha.session.DeltaManager;
import org.apache.catalina.ha.session.JvmRouteBinderValve;
import org.apache.catalina.ha.tcp.ReplicationValve;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.MessageDispatchInterceptor;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.group.interceptors.TcpPingInterceptor;
import org.apache.catalina.tribes.membership.McastService;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.catalina.tribes.transport.nio.NioReceiver;
import org.apache.catalina.tribes.transport.nio.PooledParallelSender;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

/**
 * A {@link CasTomcatServletWebServerFactory} that will configure Tomcat with clustering support based on the
 * provided {@link CasEmbeddedApacheTomcatClusteringProperties}.
 * <p>
 * CAS Implementations may use this as a base for further {@link Tomcat} customisation (eg to enable JNDI).
 *
 * @author sbearcsiro
 * @since 5.3.0
 */
@Slf4j
public class CasTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

    private final CasEmbeddedApacheTomcatClusteringProperties clusteringProperties;

    public CasTomcatServletWebServerFactory(final CasEmbeddedApacheTomcatClusteringProperties clusteringProperties) {
        this.clusteringProperties = clusteringProperties;
        configureContextForSessionClustering();
    }

    @Override
    protected TomcatWebServer getTomcatWebServer(final Tomcat tomcat) {
        configureSessionClustering(tomcat);
        return super.getTomcatWebServer(tomcat);
    }

    private void configureSessionClustering(final Tomcat tomcat) {
        if (!clusteringProperties.isSessionClusteringEnabled()) {
            LOGGER.debug("Tomcat session clustering/replication is turned off");
            return;
        }

        val cluster = new SimpleTcpCluster();
        cluster.setChannelSendOptions(clusteringProperties.getChannelSendOptions());

        val manager = getClusteringManagerInstance();
        cluster.setManagerTemplate(manager);

        val channel = new GroupChannel();

        val receiver = new NioReceiver();
        receiver.setPort(clusteringProperties.getReceiverPort());
        receiver.setTimeout(clusteringProperties.getReceiverTimeout());
        receiver.setMaxThreads(clusteringProperties.getReceiverMaxThreads());
        receiver.setAddress(clusteringProperties.getReceiverAddress());
        receiver.setAutoBind(clusteringProperties.getReceiverAutoBind());
        channel.setChannelReceiver(receiver);

        val membershipService = new McastService();
        membershipService.setPort(clusteringProperties.getMembershipPort());
        membershipService.setAddress(clusteringProperties.getMembershipAddress());
        membershipService.setFrequency(clusteringProperties.getMembershipFrequency());
        membershipService.setDropTime(clusteringProperties.getMembershipDropTime());
        membershipService.setRecoveryEnabled(clusteringProperties.isMembershipRecoveryEnabled());
        membershipService.setRecoveryCounter(clusteringProperties.getMembershipRecoveryCounter());
        membershipService.setLocalLoopbackDisabled(clusteringProperties.isMembershipLocalLoopbackDisabled());
        channel.setMembershipService(membershipService);

        val sender = new ReplicationTransmitter();
        sender.setTransport(new PooledParallelSender());
        channel.setChannelSender(sender);

        channel.addInterceptor(new TcpPingInterceptor());
        channel.addInterceptor(new TcpFailureDetector());
        channel.addInterceptor(new MessageDispatchInterceptor());

        val membership = new StaticMembershipInterceptor();
        val memberSpecs = clusteringProperties.getClusterMembers().split(",", -1);
        for (val spec : memberSpecs) {
            val memberDesc = new ClusterMemberDesc(spec);
            val member = new StaticMember();
            member.setHost(memberDesc.getAddress());
            member.setPort(memberDesc.getPort());
            member.setDomain("CAS");
            member.setUniqueId(memberDesc.getUniqueId());
            membership.addStaticMember(member);
            channel.addInterceptor(membership);
            cluster.setChannel(channel);
        }
        cluster.addValve(new ReplicationValve());
        cluster.addValve(new JvmRouteBinderValve());
        cluster.addClusterListener(new ClusterSessionListener());

        tomcat.getEngine().setCluster(cluster);
    }

    private void configureContextForSessionClustering() {
        if (!clusteringProperties.isSessionClusteringEnabled()) {
            LOGGER.debug("Tomcat session clustering/replication is turned off");
            return;
        }

        addContextCustomizers(context -> {
            val manager = getClusteringManagerInstance();
            context.setManager(manager);
            context.setDistributable(true);
        });
    }

    private ClusterManagerBase getClusteringManagerInstance() {
        val type = clusteringProperties.getManagerType().toUpperCase();
        if ("DELTA".equalsIgnoreCase(type)) {
            val manager = new DeltaManager();
            manager.setExpireSessionsOnShutdown(clusteringProperties.isExpireSessionsOnShutdown());
            manager.setNotifyListenersOnReplication(true);
            return manager;
        }
        val backupManager = new BackupManager();
        backupManager.setNotifyListenersOnReplication(true);
        return backupManager;
    }

    @Getter
    @ToString
    private static class ClusterMemberDesc {
        private static final int UNIQUE_ID_LIMIT = 255;
        private static final int UNIQUE_ID_ITERATIONS = 16;
        private String address;
        private int port;
        private String uniqueId;

        ClusterMemberDesc(final String spec) {
            val values = spec.split(":", -1);
            address = values[0];
            port = Integer.parseInt(values[1]);
            var index = Integer.parseInt(values[2]);
            if ((index < 0) || (index > UNIQUE_ID_LIMIT)) {
                throw new IllegalArgumentException("invalid unique index: must be >= 0 and < 256");
            }
            uniqueId = "{";
            for (var i = 0; i < UNIQUE_ID_ITERATIONS; i++, index++) {
                if (i != 0) {
                    uniqueId += ',';
                }
                uniqueId += index % (UNIQUE_ID_LIMIT + 1);
            }
            uniqueId += '}';
        }
    }
}
