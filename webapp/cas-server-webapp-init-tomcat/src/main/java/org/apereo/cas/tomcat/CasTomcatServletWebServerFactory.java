package org.apereo.cas.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheSslHostConfigProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatClusteringProperties;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
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
import org.apache.catalina.tribes.membership.cloud.CloudMembershipService;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.catalina.tribes.transport.nio.NioReceiver;
import org.apache.catalina.tribes.transport.nio.PooledParallelSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.autoconfigure.web.ServerProperties;
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

    private final CasConfigurationProperties casProperties;

    public CasTomcatServletWebServerFactory(final CasConfigurationProperties casProperties,
        final ServerProperties serverProperties) {
        super(serverProperties.getPort());
        if (StringUtils.isNotBlank(serverProperties.getServlet().getContextPath())) {
            setContextPath(serverProperties.getServlet().getContextPath());
        }
        this.casProperties = casProperties;

        configureConnectorForApr();
        configureContextForSessionClustering();
    }

    @Override
    protected TomcatWebServer getTomcatWebServer(final Tomcat tomcat) {
        configureSessionClustering(tomcat);
        return super.getTomcatWebServer(tomcat);
    }

    private void configureConnectorForApr() {
        val apr = casProperties.getServer().getTomcat().getApr();
        if (apr.isEnabled()) {
            LOGGER.trace("Attempting to initialize APR protocol via [{}] for port [{}]",
                Http11AprProtocol.class.getName(), getPort());
            val arpLifecycle = new AprLifecycleListener();
            setProtocol(Http11AprProtocol.class.getName());
            addContextLifecycleListeners(arpLifecycle);
            addConnectorCustomizers(c -> {
                if (c.getPort() == getPort()) {
                    LOGGER.debug("Enabling APR on connector port [{}]", c.getPort());
                    c.setSecure(true);
                    c.setScheme("https");

                    if (apr.getSslHostConfig().isEnabled()) {
                        configureConnectorForSslHostConfig(c, apr.getSslHostConfig());
                    }

                    val handler = (Http11AprProtocol) c.getProtocolHandler();
                    handler.setSSLEnabled(true);
                    if (StringUtils.isNotBlank(apr.getSslProtocol())) {
                        handler.setSSLProtocol(apr.getSslProtocol());
                    }
                    if (apr.getSslVerifyDepth() > 0) {
                        handler.setSSLVerifyDepth(apr.getSslVerifyDepth());
                    }
                    if (StringUtils.isNotBlank(apr.getSslVerifyClient())) {
                        handler.setSSLVerifyClient(apr.getSslVerifyClient());
                    }
                    if (StringUtils.isNotBlank(apr.getSslCipherSuite())) {
                        handler.setSSLCipherSuite(apr.getSslCipherSuite());
                    }
                    if (StringUtils.isNotBlank(apr.getSslPassword())) {
                        handler.setSSLPassword(apr.getSslPassword());
                    }
                    handler.setSSLDisableCompression(apr.isSslDisableCompression());
                    handler.setSSLHonorCipherOrder(apr.isSslHonorCipherOrder());

                    FunctionUtils.doIfNotNull(apr.getSslCaCertificateFile(),
                        Unchecked.consumer(f -> handler.setSSLCACertificateFile(apr.getSslCaCertificateFile().getCanonicalPath())));

                    FunctionUtils.doIfNotNull(apr.getSslCertificateFile(),
                        Unchecked.consumer(f -> handler.setSSLCertificateFile(apr.getSslCertificateFile().getCanonicalPath())));

                    FunctionUtils.doIfNotNull(apr.getSslCertificateKeyFile(),
                        Unchecked.consumer(f -> handler.setSSLCertificateKeyFile(apr.getSslCertificateKeyFile().getCanonicalPath())));

                    FunctionUtils.doIfNotNull(apr.getSslCertificateChainFile(),
                        Unchecked.consumer(f -> handler.setSSLCertificateChainFile(apr.getSslCertificateChainFile().getCanonicalPath())));

                    FunctionUtils.doIfNotNull(apr.getSslCaRevocationFile(),
                        Unchecked.consumer(f -> handler.setSSLCARevocationFile(apr.getSslCaRevocationFile().getCanonicalPath())));
                }
            });
        }
    }

    private void configureSessionClustering(final Tomcat tomcat) {
        val clusteringProperties = casProperties.getServer().getTomcat().getClustering();
        if (!clusteringProperties.isEnabled()) {
            LOGGER.trace("Tomcat session clustering/replication is turned off");
            return;
        }

        val cluster = new SimpleTcpCluster();
        val groupChannel = new GroupChannel();
        val receiver = new NioReceiver();
        receiver.setPort(clusteringProperties.getReceiverPort());
        receiver.setTimeout(clusteringProperties.getReceiverTimeout());
        receiver.setMaxThreads(clusteringProperties.getReceiverMaxThreads());
        receiver.setAddress(clusteringProperties.getReceiverAddress());
        receiver.setAutoBind(clusteringProperties.getReceiverAutoBind());
        groupChannel.setChannelReceiver(receiver);

        val sender = new ReplicationTransmitter();
        sender.setTransport(new PooledParallelSender());
        groupChannel.setChannelSender(sender);

        groupChannel.addInterceptor(new TcpPingInterceptor());
        groupChannel.addInterceptor(new TcpFailureDetector());
        groupChannel.addInterceptor(new MessageDispatchInterceptor());

        cluster.setChannelSendOptions(clusteringProperties.getChannelSendOptions());

        val manager = getClusteringManagerInstance();
        cluster.setManagerTemplate(manager);

        cluster.addValve(new ReplicationValve());
        cluster.addValve(new JvmRouteBinderValve());
        cluster.addClusterListener(new ClusterSessionListener());

        if ("CLOUD".equalsIgnoreCase(clusteringProperties.getClusteringType())) {
            val membershipService = new CloudMembershipService();
            membershipService.setMembershipProviderClassName(clusteringProperties.getCloudMembershipProvider());
            groupChannel.setMembershipService(membershipService);
            LOGGER.trace("Tomcat session clustering/replication configured using cloud membership provider [{}]",
                clusteringProperties.getCloudMembershipProvider());
        } else {
            val membershipService = new McastService();
            membershipService.setPort(clusteringProperties.getMembershipPort());
            membershipService.setAddress(clusteringProperties.getMembershipAddress());
            membershipService.setFrequency(clusteringProperties.getMembershipFrequency());
            membershipService.setDropTime(clusteringProperties.getMembershipDropTime());
            membershipService.setRecoveryEnabled(clusteringProperties.isMembershipRecoveryEnabled());
            membershipService.setRecoveryCounter(clusteringProperties.getMembershipRecoveryCounter());
            membershipService.setLocalLoopbackDisabled(clusteringProperties.isMembershipLocalLoopbackDisabled());
            groupChannel.setMembershipService(membershipService);

            val clusterMembers = clusteringProperties.getClusterMembers();
            if (StringUtils.isNotBlank(clusterMembers)) {
                val membership = new StaticMembershipInterceptor();
                val memberSpecs = clusterMembers.split(",", -1);
                for (val spec : memberSpecs) {
                    val memberDesc = new ClusterMemberDesc(spec);
                    val member = new StaticMember();
                    member.setHost(memberDesc.getAddress());
                    member.setPort(memberDesc.getPort());
                    member.setDomain("CAS");
                    member.setUniqueId(memberDesc.getUniqueId());
                    membership.addStaticMember(member);
                    groupChannel.addInterceptor(membership);
                }
            }
        }
        cluster.setChannel(groupChannel);
        tomcat.getEngine().setCluster(cluster);
    }

    private void configureContextForSessionClustering() {
        val clusteringProperties = casProperties.getServer().getTomcat().getClustering();
        if (!clusteringProperties.isEnabled()) {
            LOGGER.trace("Tomcat session clustering/replication is turned off");
            return;
        }

        addContextCustomizers(context -> {
            val manager = getClusteringManagerInstance();
            context.setManager(manager);
            context.setDistributable(true);
        });
    }

    private ClusterManagerBase getClusteringManagerInstance() {
        val clusteringProperties = casProperties.getServer().getTomcat().getClustering();
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

    private static void configureConnectorForSslHostConfig(final Connector c,
        final CasEmbeddedApacheSslHostConfigProperties properties) {

        val hostConfig = new SSLHostConfig();
        hostConfig.setCertificateVerification(properties.getCertificateVerification());
        hostConfig.setCertificateVerificationDepth(properties.getCertificateVerificationDepth());
        hostConfig.setHostName(properties.getHostName());
        hostConfig.setProtocols(properties.getProtocols());
        hostConfig.setSslProtocol(properties.getSslProtocol());
        hostConfig.setCaCertificateFile(properties.getCaCertificateFile());
        hostConfig.setInsecureRenegotiation(properties.isInsecureRenegotiation());
        hostConfig.setRevocationEnabled(properties.isRevocationEnabled());

        properties.getCertificates().forEach(cert -> {
            val certificate = new SSLHostConfigCertificate(hostConfig,
                SSLHostConfigCertificate.Type.valueOf(cert.getType().trim().toUpperCase()));
            certificate.setCertificateChainFile(cert.getCertificateChainFile());
            certificate.setCertificateFile(cert.getCertificateFile());
            certificate.setCertificateKeyFile(cert.getCertificateKeyFile());
            certificate.setCertificateKeyPassword(cert.getCertificateKeyPassword());
            hostConfig.addCertificate(certificate);
        });
        c.addSslHostConfig(hostConfig);
    }

    @Getter
    @ToString
    private static class ClusterMemberDesc {
        private static final int UNIQUE_ID_LIMIT = 255;

        private static final int UNIQUE_ID_ITERATIONS = 16;

        private final String address;

        private final int port;

        private String uniqueId;

        ClusterMemberDesc(final String spec) {
            val values = spec.split(":", -1);
            address = values[0];
            port = Integer.parseInt(values[1]);
            var index = Integer.parseInt(values[2]);
            if (index < 0 || index > UNIQUE_ID_LIMIT) {
                throw new IllegalArgumentException("Invalid unique index: must be >= 0 and < 256");
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
