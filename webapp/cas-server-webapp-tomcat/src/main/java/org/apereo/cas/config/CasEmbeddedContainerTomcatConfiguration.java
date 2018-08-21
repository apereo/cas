package org.apereo.cas.config;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.ExtendedAccessLogValve;
import org.apache.catalina.valves.SSLValve;
import org.apache.catalina.valves.rewrite.RewriteValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.ajp.AjpNio2Protocol;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.SocketUtils;

import java.io.BufferedReader;
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
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CasEmbeddedContainerTomcatConfiguration {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private CasConfigurationProperties casProperties;

    private static void configureConnectorForProtocol(final Connector connector, final String protocol) {
        val field = ReflectionUtils.findField(connector.getClass(), "protocolHandler");
        ReflectionUtils.makeAccessible(field);
        switch (protocol) {
            case "AJP/2":
                ReflectionUtils.setField(field, connector, new AjpNio2Protocol());
                break;
            case "AJP/1.3":
                ReflectionUtils.setField(field, connector, new AjpNioProtocol());
                break;
            case "HTTP/2":
                ReflectionUtils.setField(field, connector, new Http2Protocol());
                break;
            case "HTTP/1.2":
                ReflectionUtils.setField(field, connector, new Http11Nio2Protocol());
                break;
            case "HTTP/1.1":
            default:
                ReflectionUtils.setField(field, connector, new Http11NioProtocol());
                break;
        }
    }

    @ConditionalOnMissingBean(name = "casServletWebServerFactory")
    @Bean
    public ConfigurableServletWebServerFactory casServletWebServerFactory() {
        return new CasTomcatServletWebServerFactory(casProperties.getServer().getTomcat().getClustering());
    }

    @ConditionalOnMissingBean(name = "casTomcatEmbeddedServletContainerCustomizer")
    @Bean
    public ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer() {
        return new ServletWebServerFactoryCustomizer(serverProperties) {
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
                } else {
                    LOGGER.error("Servlet web server factory [{}] does not support Apache Tomcat and cannot be customized!", factory);
                }
            }
        };
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

    private void configureRewriteValve(final TomcatServletWebServerFactory tomcat) {
        val res = casProperties.getServer().getTomcat().getRewriteValve().getLocation();
        if (ResourceUtils.doesResourceExist(res)) {
            LOGGER.debug("Configuring rewrite valve at [{}]", res);

            final RewriteValve valve = new RewriteValve() {
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
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            };
            valve.setAsyncSupported(true);
            valve.setEnabled(true);

            LOGGER.debug("Creating Rewrite valve configuration for the embedded tomcat container...");
            tomcat.addContextValves(valve);
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
            tomcat.addContextValves(valve);
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

            http.getAttributes().forEach(connector::setAttribute);
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
                    configureConnectorForProtocol(connector, proxy.getProtocol());
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
}
