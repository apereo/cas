package org.apereo.cas.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatHttpProxyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.valves.ExtendedAccessLogValve;
import org.apache.catalina.valves.SSLValve;
import org.apache.catalina.valves.rewrite.RewriteValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.apache.coyote.ajp.AjpNio2Protocol;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.SocketUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasTomcatServletWebServerFactoryCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class CasTomcatServletWebServerFactoryCustomizer extends ServletWebServerFactoryCustomizer {
    private final CasConfigurationProperties casProperties;

    private final ServerProperties serverProperties;

    public CasTomcatServletWebServerFactoryCustomizer(final ServerProperties serverProperties,
                                                      final CasConfigurationProperties casProperties) {
        super(serverProperties);
        this.casProperties = casProperties;
        this.serverProperties = serverProperties;
    }

    private static void configureConnectorForProtocol(final Connector connector,
                                                      final CasEmbeddedApacheTomcatHttpProxyProperties proxy) {
        val handler = ReflectionUtils.findField(connector.getClass(), "protocolHandler");
        if (handler != null) {
            ReflectionUtils.makeAccessible(handler);
            if ("HTTP/2".equalsIgnoreCase(proxy.getProtocol())) {
                ReflectionUtils.setField(handler, connector, new Http2Protocol());
            } else {
                var protocolHandlerInstance = (AbstractProtocol) null;
                switch (proxy.getProtocol()) {
                    case "AJP/2":
                        protocolHandlerInstance = new AjpNio2Protocol();
                        val ajp1 = AbstractAjpProtocol.class.cast(protocolHandlerInstance);
                        ajp1.setSecretRequired(proxy.isSecure());
                        ajp1.setSecret(proxy.getSecret());
                        break;
                    case "AJP/1.3":
                        protocolHandlerInstance = new AjpNioProtocol();
                        val ajp2 = AbstractAjpProtocol.class.cast(protocolHandlerInstance);
                        ajp2.setSecretRequired(proxy.isSecure());
                        ajp2.setSecret(proxy.getSecret());
                        break;
                    case "APR":
                        protocolHandlerInstance = new Http11AprProtocol();
                        break;
                    case "HTTP/1.2":
                        protocolHandlerInstance = new Http11Nio2Protocol();
                        break;
                    case "HTTP/1.1":
                    default:
                        protocolHandlerInstance = new Http11NioProtocol();
                        break;
                }
                protocolHandlerInstance.setPort(connector.getPort());
                ReflectionUtils.setField(handler, connector, protocolHandlerInstance);
            }
            val handlerClass = ReflectionUtils.findField(connector.getClass(), "protocolHandlerClassName");
            if (handlerClass != null) {
                ReflectionUtils.makeAccessible(handlerClass);
                ReflectionUtils.setField(handlerClass, connector, connector.getProtocolHandler().getClass().getName());
            }
        }
    }

    @Override
    public void customize(final ConfigurableServletWebServerFactory factory) {
        if (factory instanceof TomcatServletWebServerFactory) {
            val tomcat = (TomcatServletWebServerFactory) factory;
            configureAjp(tomcat);
            configureHttp(tomcat);
            configureHttpProxy(tomcat);
            configureExtendedAccessLogValve(tomcat);
            configureRewriteValve(tomcat);
            configureSSLValve(tomcat);
            configureBasicAuthn(tomcat);
            finalizeConnectors(tomcat);
        } else {
            LOGGER.error("Servlet web server factory [{}] does not support Apache Tomcat and cannot be customized.", factory);
        }
    }

    private void finalizeConnectors(final TomcatServletWebServerFactory tomcat) {
        tomcat.addConnectorCustomizers(connector -> {
            val tc = casProperties.getServer().getTomcat();
            connector.setProperty("Server", tc.getServerName());

            val socket = tc.getSocket();
            if (socket.getBufferPool() > 0) {
                connector.setProperty("socket.bufferPool", String.valueOf(socket.getBufferPool()));
            }
            if (socket.getAppReadBufSize() > 0) {
                connector.setProperty("socket.appReadBufSize", String.valueOf(socket.getAppReadBufSize()));
            }
            if (socket.getAppWriteBufSize() > 0) {
                connector.setProperty("socket.appWriteBufSize", String.valueOf(socket.getAppWriteBufSize()));
            }
            if (socket.getPerformanceBandwidth() >= 0) {
                connector.setProperty("socket.performanceBandwidth", String.valueOf(socket.getPerformanceBandwidth()));
            }
            if (socket.getPerformanceConnectionTime() >= 0) {
                connector.setProperty("socket.performanceConnectionTime", String.valueOf(socket.getPerformanceConnectionTime()));
            }
            if (socket.getPerformanceLatency() >= 0) {
                connector.setProperty("socket.performanceLatency", String.valueOf(socket.getPerformanceLatency()));
            }
        });
    }

    private void configureBasicAuthn(final TomcatServletWebServerFactory tomcat) {
        val basic = casProperties.getServer().getTomcat().getBasicAuthn();
        if (basic.isEnabled()) {
            tomcat.addContextCustomizers(ctx -> {
                val config = new LoginConfig();
                config.setAuthMethod("BASIC");
                ctx.setLoginConfig(config);

                basic.getSecurityRoles().forEach(ctx::addSecurityRole);

                basic.getAuthRoles().forEach(r -> {
                    val constraint = new SecurityConstraint();
                    constraint.addAuthRole(r);
                    val collection = new SecurityCollection();
                    basic.getPatterns().forEach(collection::addPattern);
                    constraint.addCollection(collection);
                    ctx.addConstraint(constraint);
                });
            });
            tomcat.addContextValves(new BasicAuthenticator());
        }
    }

    private void configureExtendedAccessLogValve(final TomcatServletWebServerFactory tomcat) {
        val ext = casProperties.getServer().getTomcat().getExtAccessLog();

        if (ext.isEnabled() && StringUtils.isNotBlank(ext.getPattern())) {
            LOGGER.debug("Creating extended access log valve configuration for the embedded tomcat container...");
            val valve = new ExtendedAccessLogValve();
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
            tomcat.addEngineValves(valve);
        }
    }

    private void configureHttp(final TomcatServletWebServerFactory tomcat) {
        val http = casProperties.getServer().getTomcat().getHttp();
        if (http.isEnabled()) {
            LOGGER.debug("Creating HTTP configuration for the embedded tomcat container...");
            val connector = new Connector(http.getProtocol());
            var port = http.getPort();
            if (port <= 0) {
                LOGGER.warn("No explicit port configuration is provided to CAS. Scanning for available ports...");
                port = SocketUtils.findAvailableTcpPort();
            }
            LOGGER.info("Activated embedded tomcat container HTTP port on [{}]", port);
            connector.setPort(port);

            LOGGER.debug("Configuring embedded tomcat container for HTTP2 protocol support");
            connector.addUpgradeProtocol(new Http2Protocol());

            http.getAttributes().forEach(connector::setProperty);
            tomcat.addAdditionalTomcatConnectors(connector);
        }
    }

    private void configureHttpProxy(final TomcatServletWebServerFactory tomcat) {
        val proxy = casProperties.getServer().getTomcat().getHttpProxy();
        if (proxy.isEnabled()) {
            LOGGER.debug("Customizing HTTP proxying for connector listening on port [{}]", tomcat.getPort());
            tomcat.getTomcatConnectorCustomizers().add(connector -> {
                connector.setSecure(proxy.isSecure());
                connector.setScheme(proxy.getScheme());

                if (StringUtils.isNotBlank(proxy.getProtocol())) {
                    LOGGER.debug("Setting HTTP proxying protocol to [{}]", proxy.getProtocol());
                    configureConnectorForProtocol(connector, proxy);
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

                proxy.getAttributes().forEach(connector::setProperty);
                LOGGER.info("Configured connector listening on port [{}]", tomcat.getPort());
            });
        } else {
            LOGGER.trace("HTTP proxying is not enabled for CAS; Connector configuration for port [{}] is not modified.", tomcat.getPort());
        }
    }

    private void configureAjp(final TomcatServletWebServerFactory tomcat) {
        val ajp = casProperties.getServer().getTomcat().getAjp();
        if (ajp.isEnabled() && ajp.getPort() > 0) {
            LOGGER.debug("Creating AJP configuration for the embedded tomcat container...");
            val ajpConnector = new Connector(ajp.getProtocol());
            ajpConnector.setPort(ajp.getPort());
            ajpConnector.setSecure(ajp.isSecure());
            ajpConnector.setAllowTrace(ajp.isAllowTrace());
            ajpConnector.setScheme(ajp.getScheme());
            ajpConnector.setAsyncTimeout(Beans.newDuration(ajp.getAsyncTimeout()).toMillis());
            ajpConnector.setEnableLookups(ajp.isEnableLookups());
            ajpConnector.setMaxPostSize(ajp.getMaxPostSize());
            ajpConnector.addUpgradeProtocol(new Http2Protocol());

            val handler = (AbstractAjpProtocol) ajpConnector.getProtocolHandler();
            if (handler != null) {
                handler.setSecretRequired(ajp.isSecure());
                handler.setSecret(ajp.getSecret());
            }

            if (ajp.getProxyPort() > 0) {
                LOGGER.debug("Set AJP proxy port to [{}]", ajp.getProxyPort());
                ajpConnector.setProxyPort(ajp.getProxyPort());
            }

            if (ajp.getRedirectPort() > 0) {
                LOGGER.debug("Set AJP redirect port to [{}]", ajp.getRedirectPort());
                ajpConnector.setRedirectPort(ajp.getRedirectPort());
            }
            ajp.getAttributes().forEach(ajpConnector::setProperty);
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
    }

    private void configureSSLValve(final TomcatServletWebServerFactory tomcat) {
        val valveConfig = casProperties.getServer().getTomcat().getSslValve();

        if (valveConfig.isEnabled()) {
            LOGGER.debug("Adding SSLValve to context of the embedded tomcat container...");
            val valve = new SSLValve();
            valve.setSslCipherHeader(valveConfig.getSslCipherHeader());
            valve.setSslCipherUserKeySizeHeader(valveConfig.getSslCipherUserKeySizeHeader());
            valve.setSslClientCertHeader(valveConfig.getSslClientCertHeader());
            valve.setSslSessionIdHeader(valveConfig.getSslSessionIdHeader());
            tomcat.addContextValves(valve);
        }
    }

    private void configureRewriteValve(final TomcatServletWebServerFactory tomcat) {
        val res = casProperties.getServer().getTomcat().getRewriteValve().getLocation();
        if (ResourceUtils.doesResourceExist(res)) {
            LOGGER.debug("Configuring rewrite valve at [{}]", res);

            val valve = new RewriteValve() {
                @Override
                public synchronized void startInternal() {
                    try {
                        super.startInternal();
                        try (val is = res.getInputStream();
                             val isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                             val buffer = new BufferedReader(isr)) {
                            parse(buffer);
                        }
                    } catch (final Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
            };
            valve.setAsyncSupported(true);
            valve.setEnabled(true);

            LOGGER.debug("Creating rewrite valve configuration for the embedded tomcat container...");
            tomcat.addContextValves(valve);
        }
    }

}
