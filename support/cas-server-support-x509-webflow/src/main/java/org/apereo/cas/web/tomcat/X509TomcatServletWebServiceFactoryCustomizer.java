package org.apereo.cas.web.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

/**
 * This is {@link X509TomcatServletWebServiceFactoryCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class X509TomcatServletWebServiceFactoryCustomizer extends ServletWebServerFactoryCustomizer {
    private final CasConfigurationProperties casProperties;

    private final ServerProperties serverProperties;

    public X509TomcatServletWebServiceFactoryCustomizer(final ServerProperties serverProperties,
        final CasConfigurationProperties casProperties) {
        super(serverProperties);
        this.casProperties = casProperties;
        this.serverProperties = serverProperties;
    }

    @Override
    public void customize(final ConfigurableServletWebServerFactory factory) {
        val webflow = casProperties.getAuthn().getX509().getWebflow();
        if (factory instanceof TomcatServletWebServerFactory && webflow.getPort() > 0) {

            val tomcat = (TomcatServletWebServerFactory) factory;
            LOGGER.debug("Creating X509 configuration for the tomcat container...");
            val connector = new Connector("HTTP/1.1");
            connector.setPort(webflow.getPort());
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setAllowTrace(true);
            val protocol = (AbstractHttp11JsseProtocol) connector.getProtocolHandler();
            protocol.setSSLEnabled(true);
            protocol.setSslProtocol("TLS");
            protocol.setClientAuth(webflow.getClientAuth());
            protocol.setKeystoreFile(serverProperties.getSsl().getKeyStore());
            protocol.setKeystorePass(serverProperties.getSsl().getKeyStorePassword());
            protocol.setTruststoreFile(serverProperties.getSsl().getTrustStore());
            protocol.setTruststorePass(serverProperties.getSsl().getTrustStorePassword());
            tomcat.addAdditionalTomcatConnectors(connector);
        }
    }
}
