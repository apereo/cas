package org.apereo.cas.web.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.autoconfigure.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;

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
    private final TomcatServerProperties tomcatServerProperties;

    public X509TomcatServletWebServiceFactoryCustomizer(final ServerProperties serverProperties,
                                                        final TomcatServerProperties tomcatServerProperties,
                                                        final CasConfigurationProperties casProperties) {
        super(serverProperties);
        this.casProperties = casProperties;
        this.serverProperties = serverProperties;
        this.tomcatServerProperties = tomcatServerProperties;
    }

    @Override
    public void customize(final @NonNull ConfigurableServletWebServerFactory factory) {
        val webflow = casProperties.getAuthn().getX509().getWebflow();
        if (factory instanceof final TomcatServletWebServerFactory tomcat && webflow.getPort() > 0) {
            LOGGER.debug("Creating X509 configuration for the tomcat container...");
            val connector = new Connector("HTTP/1.1");
            connector.setPort(webflow.getPort());
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setAllowTrace(true);
            
            val maxPostSize = Long.valueOf(tomcatServerProperties.getMaxHttpFormPostSize().toBytes());
            connector.setMaxPostSize(maxPostSize.intValue());
            LOGGER.debug("Configured max post size for the tomcat connector on port [{}] to be [{}]", webflow.getPort(), maxPostSize);

            val protocol = (AbstractHttp11Protocol) connector.getProtocolHandler();
            protocol.setSSLEnabled(true);
            
            val maxHeaderSize = Long.valueOf(serverProperties.getMaxHttpRequestHeaderSize().toBytes());
            protocol.setMaxHttpRequestHeaderSize(maxHeaderSize.intValue());
            LOGGER.debug("Configured max request header size for the tomcat connector on port [{}] to be [{}]", webflow.getPort(), maxHeaderSize);
            
            val maxResponseHeader = Long.valueOf(tomcatServerProperties.getMaxHttpResponseHeaderSize().toBytes());
            protocol.setMaxHttpResponseHeaderSize(maxResponseHeader.intValue());
            LOGGER.debug("Configured max response header size for the tomcat connector on port [{}] to be [{}]",
                webflow.getPort(), maxResponseHeader);
            
            val sslHostConfig = new SSLHostConfig();
            sslHostConfig.setSslProtocol("TLS");
            sslHostConfig.setHostName(protocol.getDefaultSSLHostConfigName());
            sslHostConfig.setCertificateVerification(webflow.getClientAuth());
            val certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.UNDEFINED);

            certificate.setCertificateKeystoreFile(serverProperties.getSsl().getKeyStore());
            certificate.setCertificateKeyPassword(serverProperties.getSsl().getKeyStorePassword());
            
            sslHostConfig.setTruststoreFile(serverProperties.getSsl().getTrustStore());
            sslHostConfig.setTruststorePassword(serverProperties.getSsl().getTrustStorePassword());

            sslHostConfig.addCertificate(certificate);
            protocol.addSslHostConfig(sslHostConfig);
            tomcat.addAdditionalConnectors(connector);
        }
    }
}
