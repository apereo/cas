package org.apereo.cas.config;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Connector;
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
import org.apache.catalina.valves.ExtendedAccessLogValve;
import org.apache.catalina.valves.SSLValve;
import org.apache.catalina.valves.rewrite.RewriteValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatAjpProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatBasicAuthenticationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatClusteringProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatExtendedAccessLogProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatHttpProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatHttpProxyProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatSslValveProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.util.SocketUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasEmbeddedContainerTomcatConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casEmbeddedContainerTomcatConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE, havingValue = "true")
@ConditionalOnClass(value = {Tomcat.class, Http2Protocol.class})
@AutoConfigureBefore(EmbeddedServletContainerAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CasEmbeddedContainerTomcatConfiguration {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casServletContainerFactory")
    @Bean
    public EmbeddedServletContainerFactory casServletContainerFactory() {
        return new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(final Tomcat tomcat) {
                configureSessionClustering(tomcat);
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }
        };
    }

    @ConditionalOnMissingBean(name = "casTomcatEmbeddedServletContainerCustomizer")
    @Bean
    public EmbeddedServletContainerCustomizer casTomcatEmbeddedServletContainerCustomizer() {
        return configurableEmbeddedServletContainer -> {
            if (configurableEmbeddedServletContainer instanceof TomcatEmbeddedServletContainerFactory) {
                final TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) configurableEmbeddedServletContainer;
                configureAjp(tomcat);
                configureHttp(tomcat);
                configureHttpProxy(tomcat);
                configureExtendedAccessLogValve(tomcat);
                configureRewriteValve(tomcat);
                configureSSLValve(tomcat);
                configureBasicAuthn(tomcat);
                configureContextForSessionClustering(tomcat);
            } else {
                LOGGER.error("EmbeddedServletContainer [{}] does not support Tomcat!", configurableEmbeddedServletContainer);
            }
        };
    }

    private void configureContextForSessionClustering(final TomcatEmbeddedServletContainerFactory tomcat) {
        if (!isSessionClusteringEnabled()) {
            LOGGER.debug("Tomcat session clustering/replication is turned off");
            return;
        }

        tomcat.addContextCustomizers((TomcatContextCustomizer) context -> {
            final ClusterManagerBase manager = getClusteringManagerInstance();
            context.setManager(manager);
            context.setDistributable(true);
        });
    }

    private ClusterManagerBase getClusteringManagerInstance() {
        final CasEmbeddedApacheTomcatClusteringProperties props = casProperties.getServer().getClustering();
        switch (props.getManagerType().toUpperCase()) {
            case "DELTA":
                final DeltaManager manager = new DeltaManager();
                manager.setExpireSessionsOnShutdown(props.isExpireSessionsOnShutdown());
                manager.setNotifyListenersOnReplication(true);
                return manager;
            default:
                final BackupManager backupManager = new BackupManager();
                backupManager.setNotifyListenersOnReplication(true);
                return backupManager;
        }

    }

    private void configureBasicAuthn(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatBasicAuthenticationProperties basic = casProperties.getServer().getBasicAuthn();
        if (basic.isEnabled()) {
            tomcat.addContextCustomizers(ctx -> {
                final LoginConfig config = new LoginConfig();
                config.setAuthMethod("BASIC");
                ctx.setLoginConfig(config);

                basic.getSecurityRoles().forEach(ctx::addSecurityRole);

                basic.getAuthRoles().forEach(r -> {
                    final SecurityConstraint constraint = new SecurityConstraint();
                    constraint.addAuthRole(r);
                    final SecurityCollection collection = new SecurityCollection();
                    basic.getPatterns().forEach(collection::addPattern);
                    constraint.addCollection(collection);
                    ctx.addConstraint(constraint);
                });
            });
            tomcat.addContextValves(new BasicAuthenticator());
        }
    }

    private void configureRewriteValve(final TomcatEmbeddedServletContainerFactory tomcat) {
        final Resource res = casProperties.getServer().getRewriteValve().getLocation();
        if (ResourceUtils.doesResourceExist(res)) {
            LOGGER.debug("Configuring rewrite valve at [{}]", res);

            final RewriteValve valve = new RewriteValve() {
                @Override
                @SneakyThrows
                protected synchronized void startInternal() {
                    super.startInternal();
                    try (InputStream is = res.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                         BufferedReader buffer = new BufferedReader(isr)) {
                        parse(buffer);
                    }
                }
            };
            valve.setAsyncSupported(true);
            valve.setEnabled(true);

            LOGGER.debug("Creating Rewrite valve configuration for the embedded tomcat container...");
            tomcat.addContextValves(valve);
        }
    }

    private void configureExtendedAccessLogValve(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatExtendedAccessLogProperties ext = casProperties.getServer().getExtAccessLog();

        if (ext.isEnabled() && StringUtils.isNotBlank(ext.getPattern())) {
            LOGGER.debug("Creating extended access log valve configuration for the embedded tomcat container...");
            final ExtendedAccessLogValve valve = new ExtendedAccessLogValve();
            valve.setPattern(ext.getPattern());

            if (StringUtils.isBlank(ext.getDirectory())) {
                valve.setDirectory(serverProperties.getTomcat().getAccesslog().getDirectory());
            } else {
                valve.setDirectory(ext.getDirectory());
            }
            valve.setPrefix(ext.getPrefix());
            valve.setSuffix(ext.getSuffix());
            valve.setAsyncSupported(true);
            valve.setEnabled(true);
            valve.setRotatable(true);
            valve.setBuffered(true);
            tomcat.addContextValves(valve);
            tomcat.addEngineValves(valve);
        }
    }

    private void configureHttp(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatHttpProperties http = casProperties.getServer().getHttp();
        if (http.isEnabled()) {
            LOGGER.debug("Creating HTTP configuration for the embedded tomcat container...");
            final Connector connector = new Connector(http.getProtocol());
            int port = http.getPort();
            if (port <= 0) {
                LOGGER.warn("No explicit port configuration is provided to CAS. Scanning for available ports...");
                port = SocketUtils.findAvailableTcpPort();
            }
            LOGGER.info("Activated embedded tomcat container HTTP port on [{}]", port);
            connector.setPort(port);

            LOGGER.debug("Configuring embedded tomcat container for HTTP2 protocol support");
            connector.addUpgradeProtocol(new Http2Protocol());

            http.getAttributes().forEach(connector::setAttribute);
            tomcat.addAdditionalTomcatConnectors(connector);
        }
    }

    private void configureHttpProxy(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatHttpProxyProperties proxy = casProperties.getServer().getHttpProxy();
        if (proxy.isEnabled()) {
            LOGGER.debug("Customizing HTTP proxying for connector listening on port [{}]", tomcat.getPort());
            tomcat.getTomcatConnectorCustomizers().add(connector -> {
                connector.setSecure(proxy.isSecure());
                connector.setScheme(proxy.getScheme());

                if (StringUtils.isNotBlank(proxy.getProtocol())) {
                    LOGGER.debug("Setting HTTP proxying protocol to [{}]", proxy.getProtocol());
                    connector.setProtocol(proxy.getProtocol());
                }
                if (proxy.getRedirectPort() > 0) {
                    LOGGER.debug("Setting HTTP proxying redirect port to [{}]", proxy.getRedirectPort());
                    connector.setRedirectPort(proxy.getRedirectPort());
                }
                if (proxy.getProxyPort() > 0) {
                    LOGGER.debug("Setting HTTP proxying proxy port to [{}]", proxy.getProxyPort());
                    connector.setProxyPort(proxy.getProxyPort());
                }
                connector.addUpgradeProtocol(new Http2Protocol());

                proxy.getAttributes().forEach(connector::setAttribute);
                LOGGER.info("Configured connector listening on port [{}]", tomcat.getPort());
            });
        } else {
            LOGGER.debug("HTTP proxying is not enabled for CAS; Connector configuration for port [{}] is not modified.", tomcat.getPort());
        }
    }

    private void configureAjp(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatAjpProperties ajp = casProperties.getServer().getAjp();
        if (ajp.isEnabled() && ajp.getPort() > 0) {
            LOGGER.debug("Creating AJP configuration for the embedded tomcat container...");
            final Connector ajpConnector = new Connector(ajp.getProtocol());
            ajpConnector.setProtocol(ajp.getProtocol());
            ajpConnector.setPort(ajp.getPort());
            ajpConnector.setSecure(ajp.isSecure());
            ajpConnector.setAllowTrace(ajp.isAllowTrace());
            ajpConnector.setScheme(ajp.getScheme());
            ajpConnector.setAsyncTimeout(Beans.newDuration(ajp.getAsyncTimeout()).toMillis());
            ajpConnector.setEnableLookups(ajp.isEnableLookups());
            ajpConnector.setMaxPostSize(ajp.getMaxPostSize());
            ajpConnector.addUpgradeProtocol(new Http2Protocol());

            if (ajp.getProxyPort() > 0) {
                LOGGER.debug("Set AJP proxy port to [{}]", ajp.getProxyPort());
                ajpConnector.setProxyPort(ajp.getProxyPort());
            }

            if (ajp.getRedirectPort() > 0) {
                LOGGER.debug("Set AJP redirect port to [{}]", ajp.getRedirectPort());
                ajpConnector.setRedirectPort(ajp.getRedirectPort());
            }

            ajp.getAttributes().forEach(ajpConnector::setAttribute);

            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
    }

    private void configureSSLValve(final TomcatEmbeddedServletContainerFactory tomcat) {
        final CasEmbeddedApacheTomcatSslValveProperties valveConfig = casProperties.getServer().getSslValve();

        if (valveConfig.isEnabled()) {
            LOGGER.debug("Adding SSLValve to context of the embedded tomcat container...");
            final SSLValve valve = new SSLValve();
            valve.setSslCipherHeader(valveConfig.getSslCipherHeader());
            valve.setSslCipherUserKeySizeHeader(valveConfig.getSslCipherUserKeySizeHeader());
            valve.setSslClientCertHeader(valveConfig.getSslClientCertHeader());
            valve.setSslSessionIdHeader(valveConfig.getSslSessionIdHeader());
            tomcat.addContextValves(valve);
        }
    }

    private void configureSessionClustering(final Tomcat tomcat) {
        if (!isSessionClusteringEnabled()) {
            LOGGER.debug("Tomcat session clustering/replication is turned off");
            return;
        }

        final CasEmbeddedApacheTomcatClusteringProperties props = casProperties.getServer().getClustering();
        final SimpleTcpCluster cluster = new SimpleTcpCluster();
        cluster.setChannelSendOptions(props.getChannelSendOptions());

        final ClusterManagerBase manager = getClusteringManagerInstance();
        cluster.setManagerTemplate(manager);

        final GroupChannel channel = new GroupChannel();

        final NioReceiver receiver = new NioReceiver();
        receiver.setPort(props.getReceiverPort());
        receiver.setTimeout(props.getReceiverTimeout());
        receiver.setMaxThreads(props.getReceiverMaxThreads());
        receiver.setAddress(props.getReceiverAddress());
        receiver.setAutoBind(props.getReceiverAutoBind());
        channel.setChannelReceiver(receiver);

        final McastService membershipService = new McastService();
        membershipService.setPort(props.getMembershipPort());
        membershipService.setAddress(props.getMembershipAddress());
        membershipService.setFrequency(props.getMembershipFrequency());
        membershipService.setDropTime(props.getMembershipDropTime());
        membershipService.setRecoveryEnabled(props.isMembershipRecoveryEnabled());
        membershipService.setRecoveryCounter(props.getMembershipRecoveryCounter());
        membershipService.setLocalLoopbackDisabled(props.isMembershipLocalLoopbackDisabled());
        channel.setMembershipService(membershipService);

        final ReplicationTransmitter sender = new ReplicationTransmitter();
        sender.setTransport(new PooledParallelSender());
        channel.setChannelSender(sender);

        channel.addInterceptor(new TcpPingInterceptor());
        channel.addInterceptor(new TcpFailureDetector());
        channel.addInterceptor(new MessageDispatchInterceptor());

        final StaticMembershipInterceptor membership = new StaticMembershipInterceptor();
        final String[] memberSpecs = props.getClusterMembers().split(",", -1);
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

    private boolean isSessionClusteringEnabled() {
        final CasEmbeddedApacheTomcatClusteringProperties props = casProperties.getServer().getClustering();
        return props.isEnabled() && StringUtils.isNotBlank(props.getClusterMembers());
    }
}
