package org.apereo.cas.tomcat;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.catalina.connector.Connector;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasTomcatServletWebServerFactoryCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("ApacheTomcat")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties({
    CasConfigurationProperties.class,
    ServerProperties.class,
    TomcatServerProperties.class
})
@ExtendWith(CasTestExtension.class)
class CasTomcatServletWebServerFactoryCustomizerTests {
    @Autowired
    protected ServerProperties serverProperties;

    @Autowired
    protected TomcatServerProperties tomcatServerProperties;
    
    private static TomcatServletWebServerFactory execCustomize(final CasTomcatServletWebServerFactoryCustomizer customizer) {
        val factory = mock(TomcatServletWebServerFactory.class);
        val customizers = new HashSet<TomcatConnectorCustomizer>();
        when(factory.getConnectorCustomizers()).thenReturn(customizers);
        assertDoesNotThrow(() -> customizer.customize(factory));
        return factory;
    }

    @Test
    void verifyExtAccessLogDir() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer()
            .getTomcat()
            .getExtAccessLog()
            .setEnabled(true)
            .setPattern(".+")
            .setDirectory(FileUtils.getTempDirectoryPath());

        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        execCustomize(customizer);
    }

    @Test
    void verifyHttp2ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    void verifyAjp2ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("AJP/2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    void verifyAjp13ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("AJP/1.3");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    void verifyAprProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("APR");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    void verifyHttp12ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/1.2");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c -> c.customize(new Connector()));
    }

    @Test
    void verifyHttp11ProtocolProxy() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().getTomcat().getHttpProxy().setEnabled(true).setProtocol("HTTP/1.1");
        val customizer = new CasTomcatServletWebServerFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        val factory = execCustomize(customizer);
        factory.getConnectorCustomizers().forEach(c ->
            assertDoesNotThrow(() -> c.customize(new Connector())));
    }

}
