package org.apereo.cas.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
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
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatClusteringProperties;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

/**
 * A {@link TomcatEmbeddedServletContainerFactory} that will configure Tomcat with clustering support based on the
 * provided {@link CasEmbeddedApacheTomcatClusteringProperties}.
 *
 * CAS Implementations may use this as a base for further {@link Tomcat} customisation (eg to enable JNDI).
 *
 * @since 5.3.0
 * @author sbearcsiro
 */
@Slf4j
public class CasTomcatEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {

    private final CasEmbeddedApacheTomcatClusteringProperties clusteringProperties;

    public CasTomcatEmbeddedServletContainerFactory(final CasEmbeddedApacheTomcatClusteringProperties clusteringProperties) {
        this.clusteringProperties = clusteringProperties;
        configureContextForSessionClustering();
    }

    @Override
    protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(final Tomcat tomcat) {
        configureSessionClustering(tomcat);
        return super.getTomcatEmbeddedServletContainer(tomcat);
    }

    private void configureSessionClustering(final Tomcat tomcat) {
        if (!clusteringProperties.isSessionClusteringEnabled()) {
            LOGGER.debug("Tomcat session clustering/replication is turned off");
            return;
        }

        final SimpleTcpCluster cluster = new SimpleTcpCluster();
        cluster.setChannelSendOptions(clusteringProperties.getChannelSendOptions());

        final ClusterManagerBase manager = getClusteringManagerInstance();
        cluster.setManagerTemplate(manager);

        final GroupChannel channel = new GroupChannel();

        final NioReceiver receiver = new NioReceiver();
        receiver.setPort(clusteringProperties.getReceiverPort());
        receiver.setTimeout(clusteringProperties.getReceiverTimeout());
        receiver.setMaxThreads(clusteringProperties.getReceiverMaxThreads());
        receiver.setAddress(clusteringProperties.getReceiverAddress());
        receiver.setAutoBind(clusteringProperties.getReceiverAutoBind());
        channel.setChannelReceiver(receiver);

        final McastService membershipService = new McastService();
        membershipService.setPort(clusteringProperties.getMembershipPort());
        membershipService.setAddress(clusteringProperties.getMembershipAddress());
        membershipService.setFrequency(clusteringProperties.getMembershipFrequency());
        membershipService.setDropTime(clusteringProperties.getMembershipDropTime());
        membershipService.setRecoveryEnabled(clusteringProperties.isMembershipRecoveryEnabled());
        membershipService.setRecoveryCounter(clusteringProperties.getMembershipRecoveryCounter());
        membershipService.setLocalLoopbackDisabled(clusteringProperties.isMembershipLocalLoopbackDisabled());
        channel.setMembershipService(membershipService);

        final ReplicationTransmitter sender = new ReplicationTransmitter();
        sender.setTransport(new PooledParallelSender());
        channel.setChannelSender(sender);

        channel.addInterceptor(new TcpPingInterceptor());
        channel.addInterceptor(new TcpFailureDetector());
        channel.addInterceptor(new MessageDispatchInterceptor());

        final StaticMembershipInterceptor membership = new StaticMembershipInterceptor();
        final String[] memberSpecs = clusteringProperties.getClusterMembers().split(",", -1);
        for (final String spec : memberSpecs) {
            final ClusterMemberDesc memberDesc = new ClusterMemberDesc(spec);
            final StaticMember member = new StaticMember();
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

        addContextCustomizers((TomcatContextCustomizer) context -> {
            final ClusterManagerBase manager = getClusteringManagerInstance();
            context.setManager(manager);
            context.setDistributable(true);
        });
    }

    private ClusterManagerBase getClusteringManagerInstance() {
        switch (clusteringProperties.getManagerType().toUpperCase()) {
            case "DELTA":
                final DeltaManager manager = new DeltaManager();
                manager.setExpireSessionsOnShutdown(clusteringProperties.isExpireSessionsOnShutdown());
                manager.setNotifyListenersOnReplication(true);
                return manager;
            default:
                final BackupManager backupManager = new BackupManager();
                backupManager.setNotifyListenersOnReplication(true);
                return backupManager;
        }

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
            final String[] values = spec.split(":", -1);
            address = values[0];
            port = Integer.parseInt(values[1]);
            int index = Integer.parseInt(values[2]);
            if ((index < 0) || (index > UNIQUE_ID_LIMIT)) {
                throw new IllegalArgumentException("invalid unique index: must be >= 0 and < 256");
            }
            uniqueId = "{";
            for (int i = 0; i < UNIQUE_ID_ITERATIONS; i++, index++) {
                if (i != 0) {
                    uniqueId += ',';
                }
                uniqueId += index % (UNIQUE_ID_LIMIT + 1);
            }
            uniqueId += '}';
        }
    }
}
