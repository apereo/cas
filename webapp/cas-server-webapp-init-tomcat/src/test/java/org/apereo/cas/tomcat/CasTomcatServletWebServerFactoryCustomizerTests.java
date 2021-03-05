package org.apereo.cas.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheSslHostConfigCertificateProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheSslHostConfigProperties;

import lombok.val;
import org.apache.catalina.connector.Connector;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.http11.Http11AprProtocol;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasTomcatServletWebServerFactoryCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebApp")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
public class CasTomcatServletWebServerFactoryCustomizerTests {
    @Autowired
    protected ServerProperties serverProperties;

    @Test
    public void verifyExtAccessLogDir() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer()
            .getTomcat()
            .getExtAccessLog()
            .setEnabled(true)
            .setPattern(".+")
            .setDirectory(FileUtils.getTempDirectoryPath());

        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        execCustomize(customizer);
    }

    @Test
    public void verifyHttp2ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    public void verifyAjp2ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("AJP/2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    public void verifyAjp13ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("AJP/1.3");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    public void verifyAprProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("APR");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    public void verifyHttp12ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/1.2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    public void verifyHttp11ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/1.1");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getTomcatConnectorCustomizers().forEach(c ->
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    c.customize(new Connector());
                }
            }));
    }

    @Test
    public void verifyAprSettings() throws Exception {
        val casProperties = new CasConfigurationProperties();

        val hostConfig = new CasEmbeddedApacheSslHostConfigProperties()
            .setCaCertificateFile(File.createTempFile("cert1", ".crt").getCanonicalPath())
            .setHostName("hostfile")
            .setCertificateVerification("optional")
            .setCertificateVerificationDepth(5)
            .setCertificates(List.of(
                new CasEmbeddedApacheSslHostConfigCertificateProperties()
                    .setCertificateChainFile(File.createTempFile("cert1", ".crt").getCanonicalPath())
                    .setCertificateFile(File.createTempFile("cert1", ".crt").getCanonicalPath())
                    .setCertificateKeyFile(File.createTempFile("cert1", ".crt").getCanonicalPath())
                    .setCertificateKeyPassword("changeit")))
            .setEnabled(true);

        casProperties.getServer().getTomcat().getApr()
            .setEnabled(true)
            .setSslCaCertificateFile(File.createTempFile("cert1", ".crt"))
            .setSslCertificateFile(File.createTempFile("cert2", ".crt"))
            .setSslCertificateKeyFile(File.createTempFile("cert3", ".crt"))
            .setSslCertificateChainFile(File.createTempFile("cert4", ".crt"))
            .setSslCaRevocationFile(File.createTempFile("cert5", ".crt"))
            .setSslHostConfig(hostConfig);

        serverProperties.setPort(1234);
        val factory = new CasTomcatServletWebServerFactory(casProperties, serverProperties);
        factory.getTomcatConnectorCustomizers().forEach(c -> {
            val connector = new Connector(Http11AprProtocol.class.getCanonicalName());
            connector.setPort(serverProperties.getPort());
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    c.customize(connector);
                }
            });

        });
    }

    private static TomcatServletWebServerFactory execCustomize(final CasTomcatServletWebServerFactoryCustomizer customizer) {
        val factory = mock(TomcatServletWebServerFactory.class);
        val customizers = new ArrayList<TomcatConnectorCustomizer>();
        when(factory.getTomcatConnectorCustomizers()).thenReturn(customizers);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                customizer.customize(factory);
            }
        });
        return factory;
    }

}
