package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.rewrite.RewriteValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.AbstractProtocol;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.SocketUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is {@link CasEmbeddedContainerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casEmbeddedContainerConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEmbeddedContainerConfiguration {

    @Autowired
    private ServerProperties serverProperties;
    
    @Value("${server.tomcat.valve.rewrite.config:classpath:/container/tomcat/rewrite.config}")
    private Resource rewriteValveConfig;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnClass(Tomcat.class)
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        final TomcatEmbeddedServletContainerFactory tomcat =
                new TomcatEmbeddedServletContainerFactory();

        if (casProperties.getServer().getAjp().isEnabled()) {
            final Connector ajpConnector = new Connector(casProperties.getServer().getAjp().getProtocol());
            ajpConnector.setProtocol(casProperties.getServer().getAjp().getProtocol());
            ajpConnector.setPort(casProperties.getServer().getAjp().getPort());
            ajpConnector.setSecure(casProperties.getServer().getAjp().isSecure());
            ajpConnector.setAllowTrace(casProperties.getServer().getAjp().isAllowTrace());
            ajpConnector.setScheme(casProperties.getServer().getAjp().getScheme());
            ajpConnector.setAsyncTimeout(casProperties.getServer().getAjp().getAsyncTimeout());
            ajpConnector.setEnableLookups(casProperties.getServer().getAjp().isEnableLookups());
            ajpConnector.setMaxPostSize(casProperties.getServer().getAjp().getMaxPostSize());

            if (casProperties.getServer().getAjp().getProxyPort() > 0) {
                ajpConnector.setProxyPort(casProperties.getServer().getAjp().getProxyPort());
            }

            if (casProperties.getServer().getAjp().getRedirectPort() > 0) {
                ajpConnector.setRedirectPort(casProperties.getServer().getAjp().getRedirectPort());
            }
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }

        if (casProperties.getServer().getHttp().isEnabled()) {
            final Connector connector = new Connector(casProperties.getServer().getHttp().getProtocol());

            int port = casProperties.getServer().getHttp().getPort();
            if (port <= 0) {
                port = SocketUtils.findAvailableTcpPort();
            }
            connector.setPort(port);
            tomcat.addAdditionalTomcatConnectors(connector);
        }

        tomcat.getAdditionalTomcatConnectors()
                .stream()
                .filter(connector -> connector.getProtocolHandler() instanceof AbstractProtocol)
                .forEach(connector -> {
                    final AbstractProtocol handler = (AbstractProtocol) connector.getProtocolHandler();
                    handler.setSoTimeout(casProperties.getServer().getConnectionTimeout());
                    handler.setConnectionTimeout(casProperties.getServer().getConnectionTimeout());
                });

        if (StringUtils.isBlank(serverProperties.getContextPath())) {
            final RewriteValve valve = new RewriteValve() {
                @Override
                protected synchronized void startInternal() throws LifecycleException {
                    super.startInternal();
                    try (InputStream is = rewriteValveConfig.getInputStream();
                         BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
                        parse(buffer);
                    } catch (final Exception e) {
                        throw Throwables.propagate(e);
                    }
                }
            };
            valve.setAsyncSupported(true);
            valve.setEnabled(true);
            tomcat.addContextValves(valve);
        }
        return tomcat;
    }
}
