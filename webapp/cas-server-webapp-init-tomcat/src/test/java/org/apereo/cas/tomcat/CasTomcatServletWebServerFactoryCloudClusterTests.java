package org.apereo.cas.tomcat;

import module java.base;
import org.apereo.cas.config.CasEmbeddedContainerTomcatAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.autoconfigure.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasTomcatServletWebServerFactoryCloudClusterTests}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SpringBootTest(classes = CasEmbeddedContainerTomcatAutoConfiguration.class, properties = {
    "server.port=8183",
    "server.ssl.enabled=false"
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Tag("ApacheTomcat")
@EnableConfigurationProperties({
    CasConfigurationProperties.class,
    ServerProperties.class,
    TomcatServerProperties.class
})
@ExtendWith(CasTestExtension.class)
class CasTomcatServletWebServerFactoryCloudClusterTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    protected ServerProperties serverProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;

    @Test
    void verifyOperation() {
        casTomcatEmbeddedServletContainerCustomizer.customize(casServletWebServerFactory);
        val server = casServletWebServerFactory.getWebServer();
        assertNotNull(server);
    }


    @Test
    void verifyDynamicCloud() {
        val props = new CasConfigurationProperties();
        props.getServer().getTomcat().getClustering()
            .setEnabled(true).setClusteringType("CLOUD");

        val factory = new CasTomcatServletWebServerFactory(props, serverProperties);

        val tomcat = mock(Tomcat.class);
        when(tomcat.getEngine()).thenReturn(mock(Engine.class));
        val service = mock(Service.class);
        when(service.findConnectors()).thenReturn(new Connector[]{});

        val server = mock(Server.class);
        when(server.findServices()).thenReturn(new Service[]{service});

        val host = mock(Host.class);
        val context = mock(Context.class);
        when(context.getBaseName()).thenReturn("cas");
        when(context.getState()).thenReturn(LifecycleState.STARTED);
        when(host.findChildren()).thenReturn(new Container[]{context});

        when(tomcat.getHost()).thenReturn(host);
        when(tomcat.getService()).thenReturn(service);
        when(tomcat.getServer()).thenReturn(server);

        assertDoesNotThrow(() -> {
            factory.getTomcatWebServer(tomcat);
        });
    }

    @Test
    void verifyStaticCloud() {
        val props = new CasConfigurationProperties();
        props.getServer().getTomcat().getClustering()
            .setEnabled(true)
            .setClusteringType("STATIC")
            .setClusterMembers("127.0.0.1:1234:0,127.0.0.2:1235:0")
            .setManagerType("DEFAULT");
        val factory = new CasTomcatServletWebServerFactory(props, serverProperties);
        val tc = mock(Tomcat.class);
        when(tc.getEngine()).thenReturn(mock(Engine.class));
        val service = mock(Service.class);
        when(service.findConnectors()).thenReturn(new Connector[]{});

        val server = mock(Server.class);
        when(server.findServices()).thenReturn(new Service[]{service});

        val host = mock(Host.class);
        val context = mock(Context.class);
        when(context.getBaseName()).thenReturn("cas");
        when(context.getState()).thenReturn(LifecycleState.STARTED);
        when(host.findChildren()).thenReturn(new Container[]{context});

        when(tc.getHost()).thenReturn(host);
        when(tc.getService()).thenReturn(service);
        when(tc.getServer()).thenReturn(server);

        assertDoesNotThrow(() -> {
            factory.getTomcatWebServer(tc);
        });
    }
}
